<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <RouterView v-slot="{ Component }">
    <transition name="fade" mode="out-in">
      <component :is="Component" />
    </transition>
  </RouterView>
</template>

<style>
:root {
  /* Brand Colors */
  --primary-color: #4F46E5; /* Indigo 600 */
  --primary-light: #818CF8;
  --primary-dark: #3730A3;
  --secondary-color: #10B981; /* Emerald 500 */
  --accent-color: #F59E0B; /* Amber 500 */
  
  /* Backgrounds */
  --bg-color: #F3F4F6; /* Gray 100 */
  --surface-color: #FFFFFF;
  
  /* Text */
  --text-primary: #111827; /* Gray 900 */
  --text-secondary: #4B5563; /* Gray 600 */
  --text-light: #9CA3AF; /* Gray 400 */
  
  /* Gradients */
  --gradient-primary: linear-gradient(135deg, #4F46E5 0%, #818CF8 100%);
  --gradient-surface: linear-gradient(to bottom right, #ffffff, #f9fafb);
  
  /* Shadows */
  --shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}

body {
  margin: 0;
  font-family: 'Roboto', 'Montserrat', "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", Arial, sans-serif;
  background-color: var(--bg-color);
  color: var(--text-primary);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* Global Transition Styles */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Page Slide Transition */
.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.3s cubic-bezier(1, 0.5, 0.8, 1);
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateX(20px);
  opacity: 0;
}

/* Fade Transform Transition */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

/* Element Plus Overrides for Modern Look */
.el-button {
  font-family: 'Montserrat', sans-serif;
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.el-button:not(.is-disabled):active {
  transform: scale(0.98);
}

.el-card {
  border-radius: 12px;
  border: none;
  box-shadow: var(--shadow-sm);
  transition: box-shadow 0.3s ease, transform 0.3s ease;
}

.el-card:hover {
  box-shadow: var(--shadow-md);
}

.el-input__wrapper {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #d1d5db inset;
}

.el-input__wrapper.is-focus {
  box-shadow: 0 0 0 2px var(--primary-color) inset !important;
}

/* Scrollbar Styling */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
