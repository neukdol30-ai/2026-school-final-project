import { useEffect } from "react";
import "./ManagementModal.css";

function ManagementModal({
                             children,
                             onClose,
                             closeDisabled = false,
                         }) {
    useEffect(() => {
        const previousOverflow =
            document.body.style.overflow;

        document.body.style.overflow = "hidden";

        function handleKeyDown(event) {
            if (
                event.key === "Escape" &&
                !closeDisabled
            ) {
                onClose();
            }
        }

        window.addEventListener(
            "keydown",
            handleKeyDown,
        );

        return () => {
            document.body.style.overflow =
                previousOverflow;

            window.removeEventListener(
                "keydown",
                handleKeyDown,
            );
        };
    }, [closeDisabled, onClose]);

    function handleBackdropClick(event) {
        if (
            event.target === event.currentTarget &&
            !closeDisabled
        ) {
            onClose();
        }
    }

    return (
        <div
            className="management-modal-backdrop"
            role="presentation"
            onMouseDown={handleBackdropClick}
        >
            <div
                className="management-modal-dialog"
                role="dialog"
                aria-modal="true"
            >
                {children}
            </div>
        </div>
    );
}

export default ManagementModal;
