<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 48 48"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
  >
    <defs>
      <linearGradient id="shieldGrad" x1="0" y1="0" x2="48" y2="48" gradientUnits="userSpaceOnUse">
        <stop offset="0%" stop-color="#69b1ff" />
        <stop offset="50%" stop-color="#4096ff" />
        <stop offset="100%" stop-color="#1677ff" />
      </linearGradient>
      <linearGradient id="pulseGrad" x1="8" y1="24" x2="40" y2="24" gradientUnits="userSpaceOnUse">
        <stop offset="0%" stop-color="#ffffff" stop-opacity="0.9" />
        <stop offset="100%" stop-color="#bae0ff" stop-opacity="0.9" />
      </linearGradient>
      <linearGradient id="dropGrad" x1="24" y1="6" x2="24" y2="18" gradientUnits="userSpaceOnUse">
        <stop offset="0%" stop-color="#ff7a45" />
        <stop offset="100%" stop-color="#fa541c" />
      </linearGradient>
      <filter id="glow">
        <feGaussianBlur stdDeviation="1" result="blur" />
        <feMerge>
          <feMergeNode in="blur" />
          <feMergeNode in="SourceGraphic" />
        </feMerge>
      </filter>
    </defs>

    <path
      d="M24 4L6 12v12c0 11.1 7.7 21.5 18 24 10.3-2.5 18-12.9 18-24V12L24 4z"
      fill="url(#shieldGrad)"
      opacity="0.95"
    />

    <path
      d="M24 6L8 13.3v11.2c0 10 7 19.4 16 21.7 9-2.3 16-11.7 16-21.7V13.3L24 6z"
      fill="none"
      stroke="rgba(255,255,255,0.25)"
      stroke-width="0.8"
    />

    <path
      d="M24 10c1.5-3 4.5-3 4.5 0s-3 5-4.5 7c-1.5-2-4.5-4-4.5-7s3-3 4.5 0z"
      fill="url(#dropGrad)"
      opacity="0.9"
    />

    <path
      d="M10 25h5l2.5-5 3.5 10 3-7 2 4h3l2.5-5 3.5 10 2-4h4"
      fill="none"
      stroke="url(#pulseGrad)"
      stroke-width="2.2"
      stroke-linecap="round"
      stroke-linejoin="round"
      filter="url(#glow)"
    />

    <path
      d="M24 4L6 12v12c0 11.1 7.7 21.5 18 24 10.3-2.5 18-12.9 18-24V12L24 4z"
      fill="none"
      stroke="rgba(255,255,255,0.15)"
      stroke-width="0.5"
    />
  </svg>
</template>

<script setup>
defineProps({
  size: {
    type: [Number, String],
    default: 32
  }
})
</script>
