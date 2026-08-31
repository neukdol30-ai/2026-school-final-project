const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export async function requestLogin(
    loginId,
    password
){
    const response = await fetch(
        `${API_BASE_URL}/api/auth/login`,
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                loginId,
                password
            }),
        }
    );

    const body = await response
        .json()
        .catch(() => null);

    if (!response.ok || !body?.success) {
        const message =
            body?.error?.message
            ?? "로그인에 실패했습니다.";

        throw new Error(message);
    }

    return body.data;
}