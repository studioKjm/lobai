// Blockchain Configuration
export const BLOCKCHAIN_CONFIG = {
  // Local Hardhat Network
  localhost: {
    chainId: 1337,
    chainName: 'Hardhat Local',
    rpcUrl: 'http://127.0.0.1:8545',
    contractAddress: '0x5FbDB2315678afecb367f032d93F642f64180aa3', // Default Hardhat deployment address
    explorerUrl: null
  },

  // Polygon Mumbai Testnet
  mumbai: {
    chainId: 80001,
    chainName: 'Polygon Mumbai',
    rpcUrl: 'https://rpc-mumbai.maticvigil.com',
    contractAddress: process.env.VITE_MUMBAI_CONTRACT_ADDRESS || '',
    explorerUrl: 'https://mumbai.polygonscan.com'
  },

  // Polygon Mainnet
  polygon: {
    chainId: 137,
    chainName: 'Polygon Mainnet',
    rpcUrl: 'https://polygon-rpc.com',
    contractAddress: process.env.VITE_POLYGON_CONTRACT_ADDRESS || '',
    explorerUrl: 'https://polygonscan.com'
  }
};

// Default network (use localhost for testing)
export const DEFAULT_NETWORK = 'localhost';

export type NetworkName = keyof typeof BLOCKCHAIN_CONFIG;
