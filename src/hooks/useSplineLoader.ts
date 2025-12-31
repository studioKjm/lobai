import { useState, useRef, useCallback } from 'react';

/**
 * Custom hook for managing Spline 3D character loading and interaction
 * Handles:
 * - Scene loading with proper timing (triple RAF + timeout)
 * - Object references (Mouth Move 2, Eyes Move 2)
 * - Character state toggling (default ↔ cry)
 */
export function useSplineLoader() {
  const [splineReady, setSplineReady] = useState(false);
  const [isCrying, setIsCrying] = useState(false);

  const splineRef = useRef<any>(null);
  const mouthObjRef = useRef<any>(null);
  const eyesObjRef = useRef<any>(null);

  const onSplineLoad = useCallback((splineApp: any) => {
    splineRef.current = splineApp;

    // Find the objects by name
    try {
      const mouth = splineApp.findObjectByName('Mouth Move 2');
      const eyes = splineApp.findObjectByName('Eyes Move 2');

      if (mouth) {
        mouthObjRef.current = mouth;
      }

      if (eyes) {
        eyesObjRef.current = eyes;
      }
    } catch (error) {
      console.error('Error finding Spline objects:', error);
    }

    // Wait for Spline to fully render the 3D scene
    // Multiple animation frames ensure geometry is completely loaded
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        requestAnimationFrame(() => {
          setTimeout(() => {
            setSplineReady(true);
          }, 200);
        });
      });
    });
  }, []);

  const handleCharacterClick = useCallback(() => {
    if (!mouthObjRef.current || !eyesObjRef.current) {
      console.warn('⚠️ Objects not loaded yet. Please wait for Spline to load.');
      return;
    }

    const newState = !isCrying;
    const stateName = newState ? 'cry' : 'State';

    try {
      console.log(`🔄 Attempting to change state from "${mouthObjRef.current.state}" to "${stateName}"`);

      // Direct state property assignment
      mouthObjRef.current.state = stateName;
      eyesObjRef.current.state = stateName;

      // Verify the change
      setTimeout(() => {
        console.log(`✅ State changed! Mouth is now: "${mouthObjRef.current.state}"`);
        console.log(`✅ State changed! Eyes is now: "${eyesObjRef.current.state}"`);
      }, 100);

      setIsCrying(newState);
      console.log(`🎭 Character state: ${newState ? 'crying 😢' : 'normal 😊'}`);
    } catch (error) {
      console.error('❌ Error changing state:', error);
    }
  }, [isCrying]);

  return {
    splineReady,
    splineRef,
    mouthObjRef,
    eyesObjRef,
    isCrying,
    onSplineLoad,
    handleCharacterClick
  };
}
