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
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
  ],
}
