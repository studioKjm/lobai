import React from 'react';

interface SleepOverlayProps {
  isSleeping: boolean;
}

export const SleepOverlay: React.FC<SleepOverlayProps> = ({ isSleeping }) => {
  if (!isSleeping) return null;

  return (
    <div className="absolute inset-0 pointer-events-none">
      {/* Subtle dark overlay */}
      <div className="absolute inset-0 bg-gradient-to-t from-indigo-950/30 via-transparent to-transparent" />

      {/* Main zzz particle container - positioned above character's head */}
      <div className="absolute top-[10%] left-1/2 -translate-x-1/2 translate-x-[30%] flex flex-col items-start z-20">
        {/* First z - smallest */}
        <span
          className="text-cyan-300 font-bold text-3xl animate-zzz-1"
          style={{
            textShadow: '0 0 20px rgba(34, 211, 238, 0.8), 0 0 40px rgba(34, 211, 238, 0.4)',
          }}
        >
          z
        </span>

        {/* Second z - medium */}
        <span
          className="text-cyan-200 font-bold text-4xl animate-zzz-2 -mt-2 ml-5"
          style={{
            textShadow: '0 0 25px rgba(34, 211, 238, 0.9), 0 0 50px rgba(34, 211, 238, 0.5)',
          }}
        >
          z
        </span>

        {/* Third Z - largest */}
        <span
          className="text-white font-bold text-5xl animate-zzz-3 -mt-2 ml-10"
          style={{
            textShadow: '0 0 30px rgba(34, 211, 238, 1), 0 0 60px rgba(34, 211, 238, 0.6)',
          }}
        >
          Z
        </span>
      </div>

      {/* Secondary zzz set */}
      <div className="absolute top-[25%] left-1/2 translate-x-[50%] flex flex-col items-start opacity-70 z-20">
        <span
          className="text-blue-300 font-bold text-xl animate-zzz-1"
          style={{
            textShadow: '0 0 15px rgba(147, 197, 253, 0.7)',
            animationDelay: '1s'
          }}
        >
          z
        </span>
        <span
          className="text-blue-200 font-bold text-2xl animate-zzz-2 -mt-1 ml-3"
          style={{
            textShadow: '0 0 18px rgba(147, 197, 253, 0.8)',
            animationDelay: '1.3s'
          }}
        >
          z
        </span>
        <span
          className="text-blue-100 font-bold text-3xl animate-zzz-3 -mt-1 ml-6"
          style={{
            textShadow: '0 0 22px rgba(147, 197, 253, 0.9)',
            animationDelay: '1.6s'
          }}
        >
          Z
        </span>
      </div>

      {/* Floating particles */}
      <div className="absolute top-[8%] left-[55%] z-20">
        <div className="w-3 h-3 bg-cyan-400/70 rounded-full animate-float-particle" />
      </div>
      <div className="absolute top-[30%] left-[65%] z-20">
        <div className="w-2 h-2 bg-blue-400/80 rounded-full animate-float-particle" style={{ animationDelay: '0.5s' }} />
      </div>
      <div className="absolute top-[5%] left-[60%] z-20">
        <div className="w-2 h-2 bg-indigo-300/60 rounded-full animate-float-particle" style={{ animationDelay: '1s' }} />
      </div>
    </div>
  );
};
