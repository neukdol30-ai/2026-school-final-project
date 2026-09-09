import "../css/OutboundStatusBadge.css";

function OutboundStatusBadge({ status }) {
  let label = status;

  if (status === "DRAFT") {
    label = "작성중";
  }

  if (status === "CONFIRMED") {
    label = "확정";
  }

  return (
    <span className={`outbound-status outbound-status-${status.toLowerCase()}`}>
      {label}
    </span>
  );
}

export default OutboundStatusBadge;
