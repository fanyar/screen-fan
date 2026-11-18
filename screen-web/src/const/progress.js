export const PROGRESS_STATUS = {
  COMPLETED: 'completed',
  IN_PROGRESS: 'in_progress',
  NOT_STARTED: 'not_started',
  DELAYED: 'delayed'
}

export const PROGRESS_STATUS_LABELS = {
  [PROGRESS_STATUS.COMPLETED]: '已完成',
  [PROGRESS_STATUS.IN_PROGRESS]: '施工中',
  [PROGRESS_STATUS.NOT_STARTED]: '未开始',
  [PROGRESS_STATUS.DELAYED]: '延期'
}

export const PROGRESS_STATUS_COLORS = {
  [PROGRESS_STATUS.COMPLETED]: '#22c55e',
  [PROGRESS_STATUS.IN_PROGRESS]: '#facc15',
  [PROGRESS_STATUS.NOT_STARTED]: '#64748b',
  [PROGRESS_STATUS.DELAYED]: '#ef4444'
}

export const PROGRESS_NODE_TYPE_LABELS = {
  bridge: '桥梁',
  unit: '联',
  span: '跨',
  member: '构件'
}

export const flattenProgressNodes = (nodes, depth = 0, parentName = '') => {
  if (!nodes?.length) return []
  return nodes.flatMap(node => {
    const item = {...node, depth, path: parentName ? `${parentName} / ${node.name}` : node.name}
    return [item, ...flattenProgressNodes(node.children || [], depth + 1, item.path)]
  })
}

export const calculateProgressSummary = nodes => {
  const flattened = flattenProgressNodes(nodes)
  const childIds = new Set(flattened.flatMap(node => node.children?.map(child => child.id) || []))
  const leaves = flattened.filter(node => !childIds.has(node.id))
  const summary = {
    total: leaves.length,
    completed: 0,
    inProgress: 0,
    notStarted: 0,
    delayed: 0,
    progressPercent: 0
  }
  let weighted = 0
  let weightTotal = 0
  leaves.forEach(node => {
    if (node.displayStatus === PROGRESS_STATUS.COMPLETED) summary.completed += 1
    else if (node.displayStatus === PROGRESS_STATUS.IN_PROGRESS) summary.inProgress += 1
    else if (node.displayStatus === PROGRESS_STATUS.DELAYED) summary.delayed += 1
    else summary.notStarted += 1
    const weight = Number(node.weight || 1)
    weighted += Number(node.progressPercent || 0) * weight
    weightTotal += weight
  })
  summary.progressPercent = weightTotal ? Math.round(weighted / weightTotal) : 0
  return summary
}

export const statusAtDate = (node, date) => {
  if (!date) return node.displayStatus || node.status
  const day = String(date)
  const plannedStart = node.plannedStartDate || ''
  const plannedEnd = node.plannedEndDate || ''
  const actualStart = node.actualStartDate || ''
  const actualEnd = node.actualEndDate || ''
  if (actualEnd && day >= actualEnd) return PROGRESS_STATUS.COMPLETED
  if (actualStart && day >= actualStart) {
    return plannedEnd && day > plannedEnd
      ? PROGRESS_STATUS.DELAYED
      : PROGRESS_STATUS.IN_PROGRESS
  }
  if (plannedStart && day >= plannedStart) {
    return plannedEnd && day > plannedEnd
      ? PROGRESS_STATUS.DELAYED
      : PROGRESS_STATUS.IN_PROGRESS
  }
  return PROGRESS_STATUS.NOT_STARTED
}

export const plannedStatusAtDate = (node, date) => {
  const day = date || new Date().toISOString().slice(0, 10)
  const plannedStart = node.plannedStartDate || ''
  const plannedEnd = node.plannedEndDate || ''
  if (plannedStart && day < plannedStart) return PROGRESS_STATUS.NOT_STARTED
  if (plannedEnd && day > plannedEnd) return PROGRESS_STATUS.DELAYED
  if (plannedStart && day >= plannedStart) return PROGRESS_STATUS.IN_PROGRESS
  return node.displayStatus || node.status
}

export const deviationStatus = (actualStatus, plannedStatus) => {
  if (actualStatus === PROGRESS_STATUS.DELAYED) return PROGRESS_STATUS.DELAYED
  if (actualStatus === PROGRESS_STATUS.NOT_STARTED
      && [PROGRESS_STATUS.IN_PROGRESS, PROGRESS_STATUS.COMPLETED, PROGRESS_STATUS.DELAYED].includes(plannedStatus)) {
    return PROGRESS_STATUS.DELAYED
  }
  if (actualStatus === PROGRESS_STATUS.IN_PROGRESS && plannedStatus === PROGRESS_STATUS.NOT_STARTED) {
    return PROGRESS_STATUS.IN_PROGRESS
  }
  return actualStatus
}
