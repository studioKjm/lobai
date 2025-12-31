import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useState } from 'react';
import { useAuthStore } from '@/store/authStore';

const registerSchema = z.object({
  email: z.string().email('올바른 이메일 형식이 아닙니다'),
  username: z.string().min(2, '이름은 최소 2자 이상이어야 합니다').max(50, '이름은 50자 이하여야 합니다'),
  password: z.string().min(8, '비밀번호는 최소 8자 이상이어야 합니다'),
  passwordConfirm: z.string()
}).refine((data) => data.password === data.passwordConfirm, {
  message: "비밀번호가 일치하지 않습니다",
  path: ["passwordConfirm"]
});

type RegisterFormData = z.infer<typeof registerSchema>;

interface RegisterFormProps {
  onSuccess: () => void;
}

export function RegisterForm({ onSuccess }: RegisterFormProps) {
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const register_action = useAuthStore((state) => state.register);

  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema)
  });

  const onSubmit = async (data: RegisterFormData) => {
    setIsLoading(true);
    setErrorMessage('');

    try {
      await register_action(data.email, data.password, data.username);
      onSuccess();
    } catch (error) {
      setErrorMessage(
        error instanceof Error ? error.message : '회원가입에 실패했습니다. 다시 시도해주세요.'
      );
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      {/* Email field */}
      <div>
        <label htmlFor="register-email" className="block text-sm font-medium text-white/80 mb-2">
          이메일
        </label>
        <input
          {...register('email')}
          type="email"
          id="register-email"
          className="w-full bg-white/5 border border-white/10 rounded-lg px-4 py-3 text-white placeholder:text-white/30 focus:outline-none focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500/50 transition-all"
          placeholder="example@email.com"
          disabled={isLoading}
        />
        {errors.email && (
          <p className="text-red-400 text-sm mt-1">{errors.email.message}</p>
        )}
      </div>

      {/* Username field */}
      <div>
        <label htmlFor="username" className="block text-sm font-medium text-white/80 mb-2">
          이름
        </label>
        <input
          {...register('username')}
          type="text"
          id="username"
          className="w-full bg-white/5 border border-white/10 rounded-lg px-4 py-3 text-white placeholder:text-white/30 focus:outline-none focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500/50 transition-all"
          placeholder="홍길동"
          disabled={isLoading}
        />
        {errors.username && (
          <p className="text-red-400 text-sm mt-1">{errors.username.message}</p>
        )}
      </div>

      {/* Password field */}
      <div>
        <label htmlFor="register-password" className="block text-sm font-medium text-white/80 mb-2">
          비밀번호
        </label>
        <input
          {...register('password')}
          type="password"
          id="register-password"
          className="w-full bg-white/5 border border-white/10 rounded-lg px-4 py-3 text-white placeholder:text-white/30 focus:outline-none focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500/50 transition-all"
          placeholder="••••••••"
          disabled={isLoading}
        />
        {errors.password && (
          <p className="text-red-400 text-sm mt-1">{errors.password.message}</p>
        )}
      </div>

      {/* Password confirmation field */}
      <div>
        <label htmlFor="password-confirm" className="block text-sm font-medium text-white/80 mb-2">
          비밀번호 확인
        </label>
        <input
          {...register('passwordConfirm')}
          type="password"
          id="password-confirm"
          className="w-full bg-white/5 border border-white/10 rounded-lg px-4 py-3 text-white placeholder:text-white/30 focus:outline-none focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500/50 transition-all"
          placeholder="••••••••"
          disabled={isLoading}
        />
        {errors.passwordConfirm && (
          <p className="text-red-400 text-sm mt-1">{errors.passwordConfirm.message}</p>
        )}
      </div>

      {/* General error message */}
      {errorMessage && (
        <div className="bg-red-500/10 border border-red-500/30 rounded-lg px-4 py-3">
          <p className="text-red-400 text-sm">{errorMessage}</p>
        </div>
      )}

      {/* Submit button */}
      <button
        type="submit"
        disabled={isLoading}
        className="btn-primary w-full flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {isLoading ? (
          <>
            <svg className="animate-spin h-5 w-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            회원가입 중...
          </>
        ) : (
          '회원가입'
        )}
      </button>
    </form>
  );
}
