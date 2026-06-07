export const buildCsvContent = (header, data) => {
  const headerRow = header.join(',')
  const dataRows = data.map(row =>
    row.map(cell => {
      if (cell === null || cell === undefined) return '""'
      const str = String(cell)
      if (str.includes(',') || str.includes('"') || str.includes('\n')) {
        return `"${str.replace(/"/g, '""')}"`
      }
      return `"${str}"`
    }).join(',')
  )
  return [headerRow, ...dataRows].join('\n')
}

export const downloadCsv = (csvContent, fileName) => {
  const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = fileName
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(link.href)
}

export const exportCsv = (header, data, fileName) => {
  const csvContent = buildCsvContent(header, data)
  downloadCsv(csvContent, fileName)
}
