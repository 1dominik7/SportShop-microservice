import { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "../state/store";
import { setTokenExpiresAt, signOut } from "../state/authSlice";
import { api } from "./api";

const useTokenRefresh = () => {
  const dispatch = useDispatch();
  const isLoggedIn = useSelector((state: RootState) => state.auth.isLoggedIn);
  const tokenExpiresAt = useSelector(
    (state: RootState) => state.auth.tokenExpiresAt
  );

  useEffect(() => {
    if (!isLoggedIn || !tokenExpiresAt) return;

    const now = Date.now();
    const msBeforeExpiry = tokenExpiresAt - now;
    const refreshTime = msBeforeExpiry > 60000 ? msBeforeExpiry - 60000 : 0;

    const timeoutId = setTimeout(async () => {
      try {
        const res = await api.post("/api/v1/auth/refresh-token");
        const expiresIn = res.data.expires_in;
        const newTokenExpiresAt = Date.now() + expiresIn * 1000;

        dispatch(setTokenExpiresAt(newTokenExpiresAt));
      } catch (error: any) {
        dispatch(signOut());
        console.log(error);
        window.location.href = "/login";
      }
    }, refreshTime);

    return () => clearTimeout(timeoutId);
  }, [tokenExpiresAt, dispatch, isLoggedIn]);
};

export default useTokenRefresh;
