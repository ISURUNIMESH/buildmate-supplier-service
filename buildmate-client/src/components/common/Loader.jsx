export default function Loader({ label = 'Loading…' }) {
  return (
    <div className="loader-box">
      <div className="loader" />
      <div>{label}</div>
    </div>
  )
}
