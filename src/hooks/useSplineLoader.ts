import { useState, useRef, useCallback } from 'react';

export type CharacterState = 'State' | 'cry' | 'sleep';

/**
 * Custom hook for managing Spline 3D character loading and interaction
 * Handles:
 * - Scene loading with proper timing (triple RAF + timeout)
 * - Object references (Mouth Move 2, Eyes Move 2)
 * - Character state management (default / cry / sleep)
 */
export function useSplineLoader() {
  const [splineReady, setSplineReady] = useState(false);
  const [characterState, setCharacterState] = useState<CharacterState>('State');

  const splineRef = useRef<any>(null);
  const mouthObjRef = useRef<any>(null);
  const eyesObjRef = useRef<any>(null);
  const armAnimRef = useRef<any>(null);
  const armAnimIntervalRef = useRef<NodeJS.Timeout | null>(null);

  const onSplineLoad = useCallback((splineApp: any) => {
    // Reset ready state for fresh load
    setSplineReady(false);

    splineRef.current = splineApp;

    // Debug: Log Spline instance properties
    console.log('🔧 Spline instance keys:', Object.keys(splineApp));
    if (splineApp._runtime) {
      console.log('🔧 Runtime keys:', Object.keys(splineApp._runtime));
    }

    // Debug: List all objects in the Spline scene
    try {
      const allObjects = splineApp.getAllObjects();
      console.log('🔍 All Spline Objects:');
      allObjects.forEach((obj: any, index: number) => {
        console.log(`  [${index}] Name: "${obj.name}", Type: ${obj.type}, Has States: ${!!obj.states?.length}`);
        if (obj.states?.length) {
          console.log(`       States: ${obj.states.map((s: any) => s.name).join(', ')}`);
        }
      });
    } catch (e) {
      console.log('Could not list objects:', e);
    }

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

      // Find arm animation object
      const armAnim = splineApp.findObjectByName('NULL for Animation');
      if (armAnim) {
        armAnimRef.current = armAnim;
        console.log('✅ Found arm animation object:', armAnim.name);
      }
    } catch (error) {
      console.error('Error finding Spline objects:', error);
    }

    // Wait for Spline to fully render the 3D scene
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        requestAnimationFrame(() => {
          setTimeout(() => {
            setSplineReady(true);
            // Reset character state to default on fresh load
            setCharacterState('State');
            console.log('✅ Spline fully loaded and ready');
          }, 200);
        });
      });
    });
  }, []);

  // Set character state programmatically
  const setSplineState = useCallback((newState: CharacterState) => {
    if (!mouthObjRef.current || !eyesObjRef.current) {
      console.warn('⚠️ Objects not loaded yet. Please wait for Spline to load.');
      return;
    }

    try {
      console.log(`🔄 Changing state from "${characterState}" to "${newState}"`);

      mouthObjRef.current.state = newState;
      eyesObjRef.current.state = newState;

      // Control arm animation based on sleep state
      if (armAnimRef.current) {
        if (newState === 'sleep') {
          // Freeze arm in "Down" position during sleep
          armAnimRef.current.state = 'Down';
          console.log('💤 Arm animation frozen to "Down"');

          // Keep forcing the state to prevent Spline's auto-cycling
          if (armAnimIntervalRef.current) {
            clearInterval(armAnimIntervalRef.current);
          }
          armAnimIntervalRef.current = setInterval(() => {
            if (armAnimRef.current) {
              armAnimRef.current.state = 'Down';
            }
          }, 100);
        } else {
          // Resume normal animation
          if (armAnimIntervalRef.current) {
            clearInterval(armAnimIntervalRef.current);
            armAnimIntervalRef.current = null;
          }
          // Set to Base State to let Spline resume normal cycling
          armAnimRef.current.state = 'Base State';
          console.log('✨ Arm animation resumed');
        }
      }

      setTimeout(() => {
        console.log(`✅ State changed! Mouth: "${mouthObjRef.current.state}", Eyes: "${eyesObjRef.current.state}"`);
      }, 100);

      setCharacterState(newState);

      const stateEmoji = { 'State': '😊', 'cry': '😢', 'sleep': '😴' };
      console.log(`🎭 Character state: ${newState} ${stateEmoji[newState]}`);
    } catch (error) {
      console.error('❌ Error changing state:', error);
    }
  }, [characterState]);

  // Toggle between normal and cry (for click interaction)
  const handleCharacterClick = useCallback(() => {
    if (characterState === 'sleep') return; // Don't interrupt sleep
    const newState = characterState === 'cry' ? 'State' : 'cry';
    setSplineState(newState);
  }, [characterState, setSplineState]);

  // Sleep mode controls
  const startSleep = useCallback(() => {
    setSplineState('sleep');
  }, [setSplineState]);

  const endSleep = useCallback(() => {
    setSplineState('State');
  }, [setSplineState]);

  // Camera reset is now handled by SplineCharacter component via remount
  const resetCamera = useCallback(() => {
    console.log('📷 Camera reset requested (handled by component remount)');
  }, []);

  return {
    splineReady,
    splineRef,
    mouthObjRef,
    eyesObjRef,
    armAnimRef,
    characterState,
    isSleeping: characterState === 'sleep',
    onSplineLoad,
    handleCharacterClick,
    setSplineState,
    startSleep,
    endSleep,
    resetCamera
  };
}
