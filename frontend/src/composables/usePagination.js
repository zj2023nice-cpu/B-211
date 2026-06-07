import { ref, computed } from 'vue'

export function usePagination(initialPageSize = 10) {
  const currentPage = ref(0)
  const pageSize = ref(initialPageSize)
  const total = ref(0)

  const currentPageForDisplay = computed({
    get: () => currentPage.value + 1,
    set: (val) => {
      currentPage.value = val - 1
    }
  })

  const resetPage = () => {
    currentPage.value = 0
  }

  const setTotal = (val) => {
    total.value = val
  }

  return {
    currentPage,
    pageSize,
    total,
    currentPageForDisplay,
    resetPage,
    setTotal
  }
}
