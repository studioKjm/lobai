import { Navbar } from '@/components/common/Navbar';
import { useAuthStore } from '@/stores/authStore';

export function SettingsPage() {
  const { user } = useAuthStore();

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900">
      <Navbar />

      <div className="max-w-4xl mx-auto px-4 py-24">
        <div className="glass rounded-2xl p-8">
          <h1 className="text-3xl font-bold mb-8 bg-gradient-to-r from-cyan-400 to-purple-400 bg-clip-text text-transparent">
            Settings
          </h1>

          {/* Account Information */}
          <div className="mb-8">
            <h2 className="text-xl font-semibold mb-4">계정 정보</h2>
            <div className="space-y-4">
              <div className="flex items-center justify-between py-3 border-b border-white/10">
                <span className="text-sm opacity-70">사용자명</span>
                <span className="font-medium">{user?.username}</span>
              </div>
              <div className="flex items-center justify-between py-3 border-b border-white/10">
                <span className="text-sm opacity-70">이메일</span>
                <span className="font-medium">{user?.email}</span>
              </div>
              <div className="flex items-center justify-between py-3 border-b border-white/10">
                <span className="text-sm opacity-70">구독 등급</span>
                <span className="font-medium capitalize">{user?.subscriptionTier}</span>
              </div>
              <div className="flex items-center justify-between py-3 border-b border-white/10">
                <span className="text-sm opacity-70">역할</span>
                <span className="font-medium">{user?.role}</span>
              </div>
            </div>
          </div>

          {/* Placeholder for future settings */}
          <div className="mb-8">
            <h2 className="text-xl font-semibold mb-4">환경 설정</h2>
            <p className="text-sm opacity-50">추가 설정이 곧 제공될 예정입니다.</p>
          </div>

          {/* Danger Zone */}
          <div className="border-t border-red-500/20 pt-8">
            <h2 className="text-xl font-semibold mb-4 text-red-400">위험 구역</h2>
            <button className="px-4 py-2 bg-red-500/20 hover:bg-red-500/30 border border-red-500/50 rounded-lg text-red-400 text-sm transition-colors">
              계정 삭제
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
