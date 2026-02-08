// Web3 Utilities for Blockchain Integration
import { ethers } from 'ethers';
import { BLOCKCHAIN_CONFIG, DEFAULT_NETWORK, NetworkName } from '@/config/blockchain';
import HumanIdentityRegistryABI from '@/contracts/HumanIdentityRegistry.abi.json';

// Check if MetaMask is installed
export const isMetaMaskInstalled = (): boolean => {
  return typeof window !== 'undefined' && typeof window.ethereum !== 'undefined';
};

// Request account access
export const connectWallet = async (): Promise<string> => {
  if (!isMetaMaskInstalled()) {
    throw new Error('MetaMask가 설치되어 있지 않습니다. metamask.io에서 설치해주세요.');
  }

  try {
    const accounts = await window.ethereum.request({ method: 'eth_requestAccounts' });
    return accounts[0];
  } catch (error: any) {
    if (error.code === 4001) {
      throw new Error('MetaMask 연결이 거부되었습니다.');
    }
    throw error;
  }
};

// Get current account
export const getCurrentAccount = async (): Promise<string | null> => {
  if (!isMetaMaskInstalled()) return null;

  try {
    const accounts = await window.ethereum.request({ method: 'eth_accounts' });
    return accounts[0] || null;
  } catch (error) {
    console.error('Failed to get current account:', error);
    return null;
  }
};

// Switch network
export const switchNetwork = async (network: NetworkName): Promise<void> => {
  if (!isMetaMaskInstalled()) {
    throw new Error('MetaMask가 설치되어 있지 않습니다.');
  }

  const config = BLOCKCHAIN_CONFIG[network];
  const chainIdHex = `0x${config.chainId.toString(16)}`;

  try {
    await window.ethereum.request({
      method: 'wallet_switchEthereumChain',
      params: [{ chainId: chainIdHex }],
    });
  } catch (error: any) {
    // Chain doesn't exist, add it
    if (error.code === 4902) {
      try {
        await window.ethereum.request({
          method: 'wallet_addEthereumChain',
          params: [
            {
              chainId: chainIdHex,
              chainName: config.chainName,
              rpcUrls: [config.rpcUrl],
            },
          ],
        });
      } catch (addError) {
        throw new Error('네트워크 추가에 실패했습니다.');
      }
    } else {
      throw error;
    }
  }
};

// Get provider (read-only)
export const getProvider = (network: NetworkName = DEFAULT_NETWORK): ethers.providers.Provider => {
  const config = BLOCKCHAIN_CONFIG[network];
  return new ethers.providers.JsonRpcProvider(config.rpcUrl);
};

// Get signer (requires MetaMask)
export const getSigner = async (): Promise<ethers.Signer> => {
  if (!isMetaMaskInstalled()) {
    throw new Error('MetaMask가 설치되어 있지 않습니다.');
  }

  const provider = new ethers.providers.Web3Provider(window.ethereum);
  return provider.getSigner();
};

// Get contract instance (read-only)
export const getContract = (network: NetworkName = DEFAULT_NETWORK): ethers.Contract => {
  const config = BLOCKCHAIN_CONFIG[network];
  const provider = getProvider(network);
  return new ethers.Contract(config.contractAddress, HumanIdentityRegistryABI, provider);
};

// Get contract instance with signer (for transactions)
export const getContractWithSigner = async (network: NetworkName = DEFAULT_NETWORK): Promise<ethers.Contract> => {
  const config = BLOCKCHAIN_CONFIG[network];
  const signer = await getSigner();
  return new ethers.Contract(config.contractAddress, HumanIdentityRegistryABI, signer);
};

// Contract interaction functions

/**
 * Register a new identity on blockchain
 */
export const registerIdentity = async (hipId: string, ipfsHash: string, network: NetworkName = DEFAULT_NETWORK): Promise<ethers.ContractTransaction> => {
  const contract = await getContractWithSigner(network);
  const tx = await contract.registerIdentity(hipId, ipfsHash);
  return tx;
};

/**
 * Get identity from blockchain
 */
export const getIdentity = async (hipId: string, network: NetworkName = DEFAULT_NETWORK): Promise<any> => {
  try {
    const contract = getContract(network);
    const identity = await contract.getIdentity(hipId);

    // Handle both struct and array returns
    if (Array.isArray(identity)) {
      // Array format: [hipId, ipfsHash, owner, createdAt, updatedAt, isVerified, reputationLevel, totalInteractions]
      return {
        hipId: identity[0],
        ipfsHash: identity[1],
        owner: identity[2],
        createdAt: Number(identity[3]),
        updatedAt: Number(identity[4]),
        isVerified: identity[5],
        reputationLevel: Number(identity[6]),
        totalInteractions: Number(identity[7])
      };
    } else {
      // Struct format
      return {
        hipId: identity.hipId,
        ipfsHash: identity.ipfsHash,
        owner: identity.owner,
        createdAt: Number(identity.createdAt),
        updatedAt: Number(identity.updatedAt),
        isVerified: identity.isVerified,
        reputationLevel: Number(identity.reputationLevel),
        totalInteractions: Number(identity.totalInteractions)
      };
    }
  } catch (error) {
    console.error('Failed to get identity from blockchain:', error);
    throw error;
  }
};

/**
 * Get HIP ID by address
 */
export const getHipIdByAddress = async (address: string, network: NetworkName = DEFAULT_NETWORK): Promise<string> => {
  try {
    const contract = getContract(network);
    const hipId = await contract.getHipIdByAddress(address);
    console.log(`📍 주소 ${address}의 HIP ID:`, hipId);
    return hipId;
  } catch (error) {
    console.error('Failed to get HIP ID by address:', error);
    return '';
  }
};

/**
 * Check if identity exists
 */
export const identityExists = async (hipId: string, network: NetworkName = DEFAULT_NETWORK): Promise<boolean> => {
  try {
    const identity = await getIdentity(hipId, network);
    return identity.hipId !== '';
  } catch (error) {
    return false;
  }
};

// TypeScript declarations for window.ethereum
declare global {
  interface Window {
    ethereum?: any;
  }
}
