import React from 'react';

interface PlayOverlayProps {
  isPlaying: boolean;
}

export const PlayOverlay: React.FC<PlayOverlayProps> = ({ isPlaying }) => {
  if (!isPlaying) return null;

  return (
    <div className="absolute inset-0 pointer-events-none overflow-hidden">
      {/* Main hearts - positioned around character */}
      <div className="absolute top-[15%] left-[45%] z-20">
        <span
          className="text-4xl animate-heart-float-1"
          style={{
            filter: 'drop-shadow(0 0 10px rgba(255, 100, 150, 0.8))',
          }}
        >
          💕
        </span>
      </div>

      <div className="absolute top-[20%] left-[55%] z-20">
        <span
          className="text-3xl animate-heart-float-2"
          style={{
            filter: 'drop-shadow(0 0 12px rgba(255, 50, 100, 0.9))',
          }}
        >
          ❤️
        </span>
      </div>

      <div className="absolute top-[10%] left-[50%] z-20">
        <span
          className="text-5xl animate-heart-float-3"
          style={{
            filter: 'drop-shadow(0 0 15px rgba(255, 150, 200, 0.8))',
          }}
        >
          💗
        </span>
      </div>

      {/* Secondary hearts */}
      <div className="absolute top-[25%] left-[40%] z-20">
        <span
          className="text-2xl animate-heart-float-1 opacity-80"
          style={{
            animationDelay: '0.5s',
            filter: 'drop-shadow(0 0 8px rgba(255, 100, 150, 0.7))',
          }}
        >
          💖
        </span>
      </div>

      <div className="absolute top-[30%] left-[60%] z-20">
        <span
          className="text-2xl animate-heart-float-2 opacity-70"
          style={{
            animationDelay: '0.8s',
            filter: 'drop-shadow(0 0 8px rgba(255, 100, 150, 0.6))',
          }}
        >
          💓
        </span>
      </div>

      {/* Floating small hearts */}
      <div className="absolute top-[5%] left-[48%] z-20">
        <span className="text-xl animate-heart-particle" style={{ animationDelay: '0s' }}>
          ♥
        </span>
      </div>
      <div className="absolute top-[12%] left-[58%] z-20">
        <span className="text-lg animate-heart-particle text-pink-400" style={{ animationDelay: '0.3s' }}>
          ♥
        </span>
      </div>
      <div className="absolute top-[8%] left-[42%] z-20">
        <span className="text-sm animate-heart-particle text-red-400" style={{ animationDelay: '0.6s' }}>
          ♥
        </span>
      </div>
      <div className="absolute top-[18%] left-[52%] z-20">
        <span className="text-lg animate-heart-particle text-pink-300" style={{ animationDelay: '0.9s' }}>
          ♥
        </span>
      </div>

      {/* Sparkle effects */}
      <div className="absolute top-[15%] left-[47%] z-20">
        <div className="w-2 h-2 bg-pink-400/80 rounded-full animate-sparkle" />
      </div>
      <div className="absolute top-[22%] left-[57%] z-20">
        <div className="w-1.5 h-1.5 bg-red-300/70 rounded-full animate-sparkle" style={{ animationDelay: '0.4s' }} />
      </div>
      <div className="absolute top-[10%] left-[53%] z-20">
        <div className="w-2 h-2 bg-pink-300/90 rounded-full animate-sparkle" style={{ animationDelay: '0.7s' }} />
      </div>
    </div>
  );
};
