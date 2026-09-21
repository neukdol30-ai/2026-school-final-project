import "../css/OutboundConfirmButton.css";

function OutboundConfirmButton({ onConfirm, confirming }) {
  return (
    <button
      className="outbound-confirm-button"
      type="button"
      onClick={onConfirm}
      disabled={confirming}
    >
      {confirming ? "확정 중..." : "확정"}
    </button>
  );
}

export default OutboundConfirmButton;
