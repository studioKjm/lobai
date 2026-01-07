/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['DM Sans', 'sans-serif'],
        display: ['Syne', 'sans-serif'],
      },
      colors: {
        primary: '#00d9ff',
        secondary: '#ffb800',
      },
      keyframes: {
        orbit: {
          from: { transform: 'rotate(0deg) translateX(58px) rotate(0deg)' },
          to: { transform: 'rotate(360deg) translateX(58px) rotate(-360deg)' }
        },
        pulse: {
          '0%, 100%': { opacity: '0.5' },
          '50%': { opacity: '0.8' }
        },
        'zzz-float': {
          '0%': {
            opacity: '0',
            transform: 'translateY(0) translateX(0) scale(0.8)'
          },
          '20%': {
            opacity: '1'
          },
          '80%': {
            opacity: '1'
          },
          '100%': {
            opacity: '0',
            transform: 'translateY(-30px) translateX(10px) scale(1.1)'
          }
        },
        'float-particle': {
          '0%': {
            opacity: '0',
            transform: 'translateY(0) scale(1)'
          },
          '50%': {
            opacity: '1',
            transform: 'translateY(-15px) scale(1.2)'
          },
          '100%': {
            opacity: '0',
            transform: 'translateY(-30px) scale(0.8)'
          }
        },
        'heart-float': {
          '0%': {
            opacity: '0',
            transform: 'translateY(0) scale(0.5)'
          },
          '20%': {
            opacity: '1',
            transform: 'translateY(-10px) scale(1.1)'
          },
          '50%': {
            opacity: '1',
            transform: 'translateY(-25px) scale(1)'
          },
          '80%': {
            opacity: '0.8',
            transform: 'translateY(-40px) scale(0.9)'
          },
          '100%': {
            opacity: '0',
            transform: 'translateY(-60px) scale(0.7)'
          }
        },
        'heart-particle': {
          '0%': {
            opacity: '0',
            transform: 'translateY(0) scale(0.8)'
          },
          '30%': {
            opacity: '1',
            transform: 'translateY(-20px) scale(1.2)'
          },
          '100%': {
            opacity: '0',
            transform: 'translateY(-50px) translateX(10px) scale(0.6)'
          }
        },
        'sparkle': {
          '0%, 100%': {
            opacity: '0',
            transform: 'scale(0)'
          },
          '50%': {
            opacity: '1',
            transform: 'scale(1)'
          }
        },
        'button-pulse': {
          '0%': {
            transform: 'scale(1)',
            boxShadow: '0 0 0 0 rgba(255, 100, 150, 0.7)'
          },
          '50%': {
            transform: 'scale(1.02)',
            boxShadow: '0 0 0 10px rgba(255, 100, 150, 0)'
          },
          '100%': {
            transform: 'scale(1)',
            boxShadow: '0 0 0 0 rgba(255, 100, 150, 0)'
          }
        }
      },
      animation: {
        orbit: 'orbit 20s linear infinite',
        'pulse-slow': 'pulse 2s ease-in-out infinite',
        'zzz-1': 'zzz-float 2s ease-in-out infinite',
        'zzz-2': 'zzz-float 2s ease-in-out 0.3s infinite',
        'zzz-3': 'zzz-float 2s ease-in-out 0.6s infinite',
        'float-particle': 'float-particle 3s ease-in-out infinite',
        'heart-float-1': 'heart-float 1.5s ease-out forwards',
        'heart-float-2': 'heart-float 1.5s ease-out 0.2s forwards',
        'heart-float-3': 'heart-float 1.5s ease-out 0.4s forwards',
        'heart-particle': 'heart-particle 1.2s ease-out forwards',
        'sparkle': 'sparkle 0.8s ease-in-out forwards',
        'button-pulse': 'button-pulse 0.4s ease-out'
      }
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
  ],
}
