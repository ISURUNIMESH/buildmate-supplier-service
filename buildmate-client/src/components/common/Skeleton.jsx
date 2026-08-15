export default function Skeleton({ width = '100%', height = 16, radius = 8, className = '', style = {} }) {
  return (
    <span
      className={`skeleton ${className}`}
      style={{ width, height, borderRadius: radius, ...style }}
      aria-hidden="true"
    />
  )
}

export function SkeletonBlock({ lines = 3 }) {
  return (
    <div className="skeleton-block">
      {Array.from({ length: lines }).map((_, i) => (
        <Skeleton key={i} width={i === lines - 1 ? '72%' : '100%'} height={14} />
      ))}
    </div>
  )
}

export function SkeletonCard() {
  return (
    <div className="card skeleton-card">
      <div className="card-body">
        <Skeleton width={40} height={40} radius={12} />
        <div style={{ marginTop: 12 }}>
          <Skeleton width="45%" height={12} />
          <Skeleton width="70%" height={22} style={{ marginTop: 10 }} />
          <Skeleton width="35%" height={10} style={{ marginTop: 10 }} />
        </div>
      </div>
    </div>
  )
}
