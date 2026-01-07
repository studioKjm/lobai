import React, { useRef, useEffect, useState } from 'react';
import Spline from '@splinetool/react-spline';

interface SplineCharacterProps {
  onLoad: (splineApp: any) => void;
  onClick: () => void;
  splineReady: boolean;
  isSleeping?: boolean;
  onResetCamera?: () => void;
}

export const SplineCharacter: React.FC<SplineCharacterProps> = ({
  onLoad,
  onClick,
  splineReady,
  isSleeping = false,
  onResetCamera
}) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const [splineKey, setSplineKey] = useState(0);
  const [isResetting, setIsResetting] = useState(false);

  // Prevent scroll events from propagating to page
  useEffect(() => {
    const container = containerRef.current;
    if (!container) return;

    const preventScroll = (e: WheelEvent) => {
      e.preventDefault();
      e.stopPropagation();
    };

    const preventTouchScroll = (e: TouchEvent) => {
      e.stopPropagation();
    };

    // Use passive: false to allow preventDefault
    container.addEventListener('wheel', preventScroll, { passive: false });
    container.addEventListener('touchmove', preventTouchScroll, { passive: true });

    return () => {
      container.removeEventListener('wheel', preventScroll);
      container.removeEventListener('touchmove', preventTouchScroll);
    };
  }, []);

  // Handle reset by remounting Spline component
  const handleReset = () => {
    setIsResetting(true);
    // Increment key to force remount
    setSplineKey(prev => prev + 1);
    // Reset flag after a short delay
    setTimeout(() => {
      setIsResetting(false);
    }, 500);
  };

  return (
    <div
      ref={containerRef}
      className="absolute inset-0 spline-container flex items-center justify-center overflow-hidden isolate"
      onClick={onClick}
      style={{
        touchAction: 'none',
        overscrollBehavior: 'contain'
      }}
    >
      <div
        className="w-full h-full transition-opacity duration-700 ease-out"
        style={{
          opacity: (splineReady && !isResetting) ? 1 : 0,
          visibility: (splineReady && !isResetting) ? 'visible' : 'hidden'
        }}
      >
        <Spline
          key={splineKey}
          scene="/scene.splinecode"
          onLoad={onLoad}
          className="w-full h-full cursor-pointer"
        />
      </div>

      {/* Loading indicator during reset */}
      {isResetting && (
        <div className="absolute inset-0 flex items-center justify-center">
          <div className="w-8 h-8 border-2 border-white/30 border-t-white/80 rounded-full animate-spin" />
        </div>
      )}

      {/* Subtle overlay for styling integration - exclude button area */}
      <div className="absolute inset-0 pointer-events-none bg-gradient-to-t from-[#050505] via-transparent to-transparent opacity-40" />

      {/* Reset camera button - positioned above overlay */}
      {splineReady && !isResetting && (
        <button
          onClick={(e) => {
            e.stopPropagation();
            handleReset();
          }}
          className="absolute bottom-6 right-6 z-20 px-3 py-2 rounded-lg bg-black/60 hover:bg-black/80 backdrop-blur-md border border-white/30 text-white/90 hover:text-white text-xs font-medium transition-all flex items-center gap-2 shadow-lg"
          title="캐릭터 위치 재설정"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8" />
            <path d="M3 3v5h5" />
          </svg>
          위치 재설정
        </button>
      )}
    </div>
  );
};
