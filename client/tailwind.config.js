/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      fontFamily:{
        montserrat: ['Montserrat', 'sans-serif'],
      },
      colors:{
        // "primary-color":"#00927c",
        "secondary-color":"#6BE140"
      },
    },
  },
  plugins: [],
}
