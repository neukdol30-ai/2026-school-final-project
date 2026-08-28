import {
    Navigate,
    Outlet,
    useLocation
} from "react-router-dom";

import {
    hasValidAuthSession
} from "../../store/authStorage.js";

function ProtectedRoute() {
    const location = useLocation();

    if (!hasValidAuthSession()) {
        return (
            <Navigate
                to="/login"
                replace
                state={{
                    from: location.pathname
                }}
            />
        );
    }

    return <Outlet />;
}

export default ProtectedRoute;