// Blockchain Integration Component
import { useState, useEffect } from 'react';
import toast from 'react-hot-toast';
import type { HIPProfile } from '@/types/hip';
import { BLOCKCHAIN_CONFIG, DEFAULT_NETWORK, NetworkName } from '@/config/blockchain';
import {
  isMetaMaskInstalled,
  connectWallet,
  getCurrentAccount,
  switchNetwork,
  registerIdentity,
  getIdentity,
  identityExists,
  getHipIdByAddress
} from '@/utils/web3';

interface BlockchainSectionProps {
  profile: HIPProfile;
  className?: string;
}

interface BlockchainIdentity {
  hipId: string;
  ipfsHash: string;
  owner: string;
  createdAt: number;
  updatedAt: number;
  isVerified: boolean;
  reputationLevel: number;
  totalInteractions: number;
}

export function BlockchainSection({ profile, className = '' }: BlockchainSectionProps) {
  const [account, setAccount] = useState<string | null>(null);
  const [network, setNetwork] = useState<NetworkName>(DEFAULT_NETWORK);
  const [isRegistered, setIsRegistered] = useState(false);
  const [blockchainIdentity, setBlockchainIdentity] = useState<BlockchainIdentity | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isChecking, setIsChecking] = useState(true);

  // Check if identity exists on blockchain
  useEffect(() => {
    checkBlockchainIdentity();
  }, [profile.hipId, network, account]);

  // Listen to account changes
  useEffect(() => {
    if (isMetaMaskInstalled()) {
      window.ethereum.on('accountsChanged', handleAccountsChanged);
      window.ethereum.on('chainChanged', () => window.location.reload());

      // Get current account
      getCurrentAccount().then(setAccount);

      return () => {
        window.ethereum.removeListener('accountsChanged', handleAccountsChanged);
      };
    }
  }, []);

  const handleAccountsChanged = (accounts: string[]) => {
    if (accounts.length === 0) {
      setAccount(null);
      setIsRegistered(false);
      setBlockchainIdentity(null);
      toast.error('MetaMask 연결이 해제되었습니다');
    } else {
      setAccount(accounts[0]);
      toast.success('계정이 변경되었습니다');
      // Re-check identity with new account
      setTimeout(() => {
        checkBlockchainIdentity();
      }, 500);
    }
  };

  const checkBlockchainIdentity = async () => {
    setIsChecking(true);
    try {
      // If account is connected, check by address first
      if (account) {
        const hipIdFromChain = await getHipIdByAddress(account, network);

        if (hipIdFromChain && hipIdFromChain !== '') {
          // Identity exists on blockchain
          setIsRegistered(true);
          const identity = await getIdentity(hipIdFromChain, network);
          setBlockchainIdentity(identity);
          console.log('✅ 블록체인 신원 발견:', hipIdFromChain);
          return;
        }
      }

      // Fallback: check by profile HIP ID
      const exists = await identityExists(profile.hipId, network);
      setIsRegistered(exists);

      if (exists) {
        const identity = await getIdentity(profile.hipId, network);
        setBlockchainIdentity(identity);
      } else {
        setIsRegistered(false);
        setBlockchainIdentity(null);
      }
    } catch (error) {
      console.error('Failed to check blockchain identity:', error);
      setIsRegistered(false);
      setBlockchainIdentity(null);
    } finally {
      setIsChecking(false);
    }
  };

  const handleConnectWallet = async () => {
    try {
      const connectedAccount = await connectWallet();
      setAccount(connectedAccount);
      toast.success('MetaMask 연결 성공!');

      // Check blockchain identity after connection
      setTimeout(() => {
        checkBlockchainIdentity();
      }, 500);
    } catch (error: any) {
      console.error('Failed to connect wallet:', error);
      toast.error(error.message || 'MetaMask 연결에 실패했습니다');
    }
  };

  const handleSwitchNetwork = async (newNetwork: NetworkName) => {
    try {
      await switchNetwork(newNetwork);
      setNetwork(newNetwork);
      toast.success(`${BLOCKCHAIN_CONFIG[newNetwork].chainName}으로 전환되었습니다`);
    } catch (error: any) {
      console.error('Failed to switch network:', error);
      toast.error(error.message || '네트워크 전환에 실패했습니다');
    }
  };

  const handleRegisterOnBlockchain = async () => {
    if (!account) {
      toast.error('먼저 MetaMask를 연결해주세요');
      return;
    }

    setIsLoading(true);
    const loadingToast = toast.loading('블록체인에 신원을 등록하는 중...');

    try {
      // Generate IPFS hash (mock for now - in production, upload to IPFS first)
      const ipfsHash = `Qm${Math.random().toString(36).substring(2, 15)}`;

      const tx = await registerIdentity(profile.hipId, ipfsHash, network);
      toast.loading('트랜잭션 확인 중...', { id: loadingToast });

      const receipt = await tx.wait();

      console.log('✅ 트랜잭션 완료:', receipt.transactionHash);

      toast.success('블록체인 등록 완료!', { id: loadingToast, duration: 5000 });

      // Force refresh identity after short delay
      setTimeout(async () => {
        console.log('🔄 블록체인 신원 재조회 중...');
        await checkBlockchainIdentity();
      }, 1000);
    } catch (error: any) {
      console.error('Failed to register on blockchain:', error);
      toast.error(error.message || '블록체인 등록에 실패했습니다', { id: loadingToast });
    } finally {
      setIsLoading(false);
    }
  };

  if (!isMetaMaskInstalled()) {
    return (
      <div className={`bg-gradient-to-br from-orange-50 to-red-50 dark:from-gray-800 dark:to-gray-900 rounded-2xl shadow-xl p-8 ${className}`}>
        <div className="text-center">
          <div className="text-6xl mb-4">🦊</div>
          <h3 className="text-2xl font-bold text-gray-900 dark:text-white mb-4">
            MetaMask가 필요합니다
          </h3>
          <p className="text-gray-600 dark:text-gray-400 mb-6">
            블록체인 기능을 사용하려면 MetaMask 지갑을 설치해주세요.
          </p>
          <a
            href="https://metamask.io/download/"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-block px-6 py-3 bg-orange-500 text-white rounded-lg font-semibold hover:bg-orange-600 transition"
          >
            MetaMask 설치하기
          </a>
        </div>
      </div>
    );
  }

  return (
    <div className={`bg-gradient-to-br from-blue-50 to-indigo-50 dark:from-gray-800 dark:to-gray-900 rounded-2xl shadow-xl p-8 ${className}`}>
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <div className="text-4xl">⛓️</div>
          <div>
            <h3 className="text-2xl font-bold text-gray-900 dark:text-white">
              블록체인 신원
            </h3>
            <p className="text-sm text-gray-600 dark:text-gray-400">
              탈중앙화 신원 검증
            </p>
          </div>
        </div>

        {isRegistered && (
          <span className="px-4 py-2 bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200 rounded-full text-sm font-semibold">
            ✅ 등록됨
          </span>
        )}
      </div>

      {/* Network Selector */}
      <div className="mb-6">
        <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
          네트워크 선택
        </label>
        <div className="flex gap-2">
          {(['localhost', 'mumbai'] as NetworkName[]).map((net) => (
            <button
              key={net}
              onClick={() => handleSwitchNetwork(net)}
              className={`px-4 py-2 rounded-lg font-semibold text-sm transition ${
                network === net
                  ? 'bg-indigo-600 text-white'
                  : 'bg-white dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-600'
              }`}
            >
              {BLOCKCHAIN_CONFIG[net].chainName}
            </button>
          ))}
        </div>
      </div>

      {/* Wallet Connection */}
      {!account ? (
        <div className="bg-white dark:bg-gray-800 rounded-xl p-6 mb-6">
          <p className="text-gray-600 dark:text-gray-400 mb-4 text-center">
            블록체인 기능을 사용하려면 지갑을 연결하세요
          </p>
          <button
            onClick={handleConnectWallet}
            className="w-full px-6 py-3 bg-gradient-to-r from-orange-500 to-red-500 text-white rounded-lg font-semibold hover:from-orange-600 hover:to-red-600 transition"
          >
            🦊 MetaMask 연결
          </button>
        </div>
      ) : (
        <div className="bg-white dark:bg-gray-800 rounded-xl p-6 mb-6">
          <div className="flex items-center justify-between mb-4">
            <span className="text-sm text-gray-600 dark:text-gray-400">연결된 계정</span>
            <span className="px-3 py-1 bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200 rounded-full text-xs font-semibold">
              연결됨
            </span>
          </div>
          <p className="font-mono text-sm text-gray-900 dark:text-white break-all">
            {account}
          </p>
        </div>
      )}

      {/* Blockchain Identity Status */}
      {isChecking ? (
        <div className="bg-white dark:bg-gray-800 rounded-xl p-6 text-center">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-indigo-600 border-t-transparent mb-2"></div>
          <p className="text-gray-600 dark:text-gray-400">블록체인 확인 중...</p>
        </div>
      ) : isRegistered && blockchainIdentity ? (
        <div className="bg-white dark:bg-gray-800 rounded-xl p-6">
          <h4 className="text-lg font-bold text-gray-900 dark:text-white mb-4">
            블록체인 신원 정보
          </h4>

          <div className="space-y-3">
            <div className="flex justify-between">
              <span className="text-gray-600 dark:text-gray-400">HIP ID</span>
              <span className="font-mono text-sm text-indigo-600 dark:text-indigo-400">
                {blockchainIdentity.hipId}
              </span>
            </div>

            <div className="flex justify-between">
              <span className="text-gray-600 dark:text-gray-400">검증 상태</span>
              <span className={`px-3 py-1 rounded-full text-xs font-semibold ${
                blockchainIdentity.isVerified
                  ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
                  : 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
              }`}>
                {blockchainIdentity.isVerified ? '검증됨' : '미검증'}
              </span>
            </div>

            <div className="flex justify-between">
              <span className="text-gray-600 dark:text-gray-400">평판 레벨</span>
              <span className="text-lg font-bold text-purple-600 dark:text-purple-400">
                {blockchainIdentity.reputationLevel} / 5
              </span>
            </div>

            <div className="flex justify-between">
              <span className="text-gray-600 dark:text-gray-400">블록체인 상호작용</span>
              <span className="font-semibold text-gray-900 dark:text-white">
                {blockchainIdentity.totalInteractions}회
              </span>
            </div>

            <div className="flex justify-between">
              <span className="text-gray-600 dark:text-gray-400">소유자</span>
              <span className="font-mono text-xs text-gray-600 dark:text-gray-400">
                {blockchainIdentity.owner.substring(0, 6)}...{blockchainIdentity.owner.substring(38)}
              </span>
            </div>

            <div className="flex justify-between">
              <span className="text-gray-600 dark:text-gray-400">등록일</span>
              <span className="text-sm text-gray-600 dark:text-gray-400">
                {new Date(blockchainIdentity.createdAt * 1000).toLocaleDateString('ko-KR')}
              </span>
            </div>
          </div>

          {BLOCKCHAIN_CONFIG[network].explorerUrl && (
            <a
              href={`${BLOCKCHAIN_CONFIG[network].explorerUrl}/address/${BLOCKCHAIN_CONFIG[network].contractAddress}`}
              target="_blank"
              rel="noopener noreferrer"
              className="mt-4 block w-full px-4 py-2 bg-indigo-100 dark:bg-indigo-900/30 text-indigo-600 dark:text-indigo-400 text-center rounded-lg hover:bg-indigo-200 dark:hover:bg-indigo-900/50 transition"
            >
              🔍 Explorer에서 보기
            </a>
          )}
        </div>
      ) : (
        <div className="bg-white dark:bg-gray-800 rounded-xl p-6">
          <div className="text-center mb-6">
            <div className="text-5xl mb-4">🚀</div>
            <h4 className="text-lg font-bold text-gray-900 dark:text-white mb-2">
              블록체인에 신원 등록
            </h4>
            <p className="text-sm text-gray-600 dark:text-gray-400">
              당신의 HIP 신원을 불변의 블록체인에 기록하세요.
            </p>
          </div>

          <button
            onClick={handleRegisterOnBlockchain}
            disabled={!account || isLoading}
            className={`w-full px-6 py-4 bg-gradient-to-r from-indigo-600 to-purple-600 text-white rounded-xl font-semibold text-lg shadow-lg transition-all ${
              !account || isLoading
                ? 'opacity-50 cursor-not-allowed'
                : 'hover:shadow-xl hover:scale-105'
            }`}
          >
            {isLoading ? (
              <span className="flex items-center justify-center gap-2">
                <div className="inline-block animate-spin rounded-full h-5 w-5 border-2 border-white border-t-transparent"></div>
                등록 중...
              </span>
            ) : (
              '⛓️ 블록체인에 등록하기'
            )}
          </button>

          {!account && (
            <p className="mt-4 text-center text-sm text-gray-500 dark:text-gray-500">
              먼저 지갑을 연결해주세요
            </p>
          )}
        </div>
      )}

      {/* Info */}
      <div className="mt-6 p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
        <h5 className="font-semibold text-blue-900 dark:text-blue-300 mb-2 flex items-center gap-2">
          <span>ℹ️</span>
          블록체인 신원이란?
        </h5>
        <p className="text-sm text-blue-800 dark:text-blue-400">
          당신의 HIP 점수와 신원 정보가 분산 원장에 영구적으로 기록됩니다.
          누구나 검증할 수 있지만 아무도 변조할 수 없는 투명하고 안전한 신원 증명입니다.
        </p>
      </div>
    </div>
  );
}
