const ACCESS_TOKEN_KEY =
    "foodErp.accessToken";

const USER_SESSION_KEY =
    "foodErp.userSession";

export function saveAuthSession(loginData) {
    const expiresAt =
        Date.now() + loginData.expiresIn * 1000;

    const userSession = {
        appUserId: loginData.appUserId,
        companyId: loginData.companyId,
        loginId: loginData.loginId,
        userName: loginData.userName,
        expiresAt,
    };

    localStorage.setItem(
        ACCESS_TOKEN_KEY,
        loginData.accessToken
    );

    localStorage.setItem(
        USER_SESSION_KEY,
        JSON.stringify(userSession)
    );
}

export function getAccessToken() {
    return localStorage.getItem(
        ACCESS_TOKEN_KEY
    );
}

export function getUserSession() {
    const savedSession =
        localStorage.getItem(USER_SESSION_KEY);

    if (!savedSession) {
        return null;
    }

    try {
        return JSON.parse(savedSession);
    } catch {
        clearAuthSession();
        return null;
    }
}

export function hasValidAuthSession() {
    const accessToken = getAccessToken();
    const userSession = getUserSession();

    if (!accessToken || !userSession) {
        return false;
    }

    if (Date.now() >= userSession.expiresAt) {
        clearAuthSession();
        return false;
    }

    return true;
}

export function clearAuthSession() {
    localStorage.removeItem(
        ACCESS_TOKEN_KEY
    );

    localStorage.removeItem(
        USER_SESSION_KEY
    );
}