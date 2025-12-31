import React from 'react';
import Spline from '@splinetool/react-spline';

interface SplineCharacterProps {
  onLoad: (splineApp: any) => void;
  onClick: () => void;
  splineReady: boolean;
}

export const SplineCharacter: React.FC<SplineCharacterProps> = ({
  onLoad,
  onClick,
  splineReady
}) => {
  return (
    <div
      className="flex-1 w-full h-[50vh] lg:h-[600px] relative spline-container order-1 lg:order-2 flex items-center justify-center"
      onClick={onClick}
    >
      <div
        className="w-full h-full transition-opacity duration-700 ease-out"
        style={{
          opacity: splineReady ? 1 : 0,
          visibility: splineReady ? 'visible' : 'hidden'
        }}
      >
        <Spline
          scene="/scene.splinecode"
          onLoad={onLoad}
          className="w-full h-full cursor-pointer"
        />
      </div>
      {/* Subtle overlay for styling integration */}
      <div className="absolute inset-0 pointer-events-none bg-gradient-to-t from-[#050505] via-transparent to-transparent opacity-40" />
    </div>
  );
};
