import {
  fetchUserProfileFailure,
  fetchUserProfileRequest,
  fetchUserProfileSuccess,
  forgotPasswordFailure,
  forgotPasswordRequest,
  forgotPasswordSuccess,
  resetPasswordFailure,
  resetPasswordRequest,
  resetPasswordSuccess,
  signInFailure,
  signInRequest,
  signInSuccess,
  signOutSuccess,
  signUpFailure,
  signUpRequest,
  signUpSuccess,
  verifyAccountFailure,
  verifyAccountRequest,
  verifyAccountSuccess,
} from "./authSlice";
import { api } from "../config/api";
import { SignInPayload, SignUpPayload } from "../types/userTypes";
import { ThunkAction, ThunkDispatch, UnknownAction } from "@reduxjs/toolkit";
import { NavigateFunction } from "react-router";
import { toast, ToastOptions } from "react-toastify";
import store, { RootState } from "./store";


export const toastCustomize: ToastOptions = {
  position: "bottom-right",
  autoClose: 3000,
  hideProgressBar: false,
  closeOnClick: true,
  pauseOnHover: true,
};

api.interceptors.response.use(
  (res) => res,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        await api.post("/api/v1/auth/refresh-token");
        return api(originalRequest);
      } catch (err) {
        store.dispatch(signOutSuccess());
        window.location.href = "/login";
        return Promise.reject(err);
      }
    }

    return Promise.reject(error);
  }
);

export const signUp =
  (payload: SignUpPayload, navigate: NavigateFunction) =>
  async (dispatch: ThunkDispatch<RootState, unknown, UnknownAction>) => {
    const { email, firstname, lastname, password } = payload;
    dispatch(signUpRequest());

    try {
      await api.post("/api/v1/auth/register", {
        email,
        firstName: firstname,
        lastName: lastname,
        password,
      });
      dispatch(signUpSuccess());
      toast.success("Account has been created successfully!", toastCustomize);
      navigate("/login");
    } catch (error: any) {
      dispatch(
        signUpFailure(
          error.response?.data?.errorMessage ||
            error.response?.data?.message ||
            "Sign-up failed"
        )
      );
    }
  };

export const verifyAccount =
  (tokenCode: string, navigate: NavigateFunction) =>
  async (dispatch: ThunkDispatch<RootState, unknown, UnknownAction>) => {
    dispatch(verifyAccountRequest());
    try {
      const res = await api.get(
        `/api/v1/auth/activate-account?token=${tokenCode}`
      );
      const { user } = res.data;
      dispatch(
        verifyAccountSuccess({
          user,
        })
      );
      navigate("/");
    } catch (error: any) {
      dispatch(
        verifyAccountFailure(
          error.response?.data?.error || "Account verification failed."
        )
      );
    }
  };

export const signIn =
  (payload: SignInPayload, navigate: NavigateFunction) =>
  async (dispatch: ThunkDispatch<RootState, unknown, UnknownAction>) => {
    const { email, password } = payload;
    dispatch(signInRequest());
    try {
      const res = await api.post("/api/v1/auth/authenticate", {
        email,
        password,
      });

      const { user, expiresIn } = res.data;
      const tokenExpiresAt = Date.now() + expiresIn * 1000;

      dispatch(
        signInSuccess({
          user,
          tokenExpiresAt,
        })
      );

      navigate("/");
    } catch (error: any) {
      if (error.response) {
        console.error("Error Response:", error.response.data);
        if (
          error.response.status === 500 &&
          error.response.data.error ===
            "Account is disabled. A new activation email has been sent."
        ) {
          dispatch(
            signInFailure(
              error.response?.data?.error ||
                "Your account is disabled. Please check your email for a new verification code."
            )
          );
          navigate("/verify-account", { state: { email } });
        } else {
          dispatch(
            signInFailure(
              error.response?.data?.error ||
                "Something went wrong, please try again."
            )
          );
        }
      }
    }
  };

export const forgotPassword =
  (email: string, navigate: NavigateFunction) =>
  async (dispatch: ThunkDispatch<RootState, unknown, UnknownAction>) => {
    dispatch(forgotPasswordRequest());
    try {
      const res = await api.post("/api/v1/auth/forgot-password", { email });
      const token = res.data.token;
      dispatch(forgotPasswordSuccess());
      navigate(`/reset-password/${token}`, { state: { email } });
    } catch (error: any) {
      console.log(error);
      dispatch(
        forgotPasswordFailure(
          error?.response?.data?.error || "Password reset request failed"
        )
      );
    }
  };

export const resetPassword =
  (
    {
      token,
      newPassword,
      confirmPassword,
    }: { token: string; newPassword: string; confirmPassword: string },
    navigate: NavigateFunction
  ) =>
  async (dispatch: ThunkDispatch<RootState, unknown, UnknownAction>) => {
    dispatch(resetPasswordRequest());
    try {
      await api.post("/api/v1/auth/reset-password", {
        token,
        newPassword,
        confirmPassword,
      });
      dispatch(resetPasswordSuccess());
      navigate("/login");
      toast.success("Password reset successfully!", toastCustomize);
    } catch (error: any) {
      dispatch(
        resetPasswordFailure(
          error.response?.data?.error || "Password reset failed"
        )
      );
    }
  };

export const fetchUserProfile =
  (jwt: string) =>
  async (dispatch: ThunkDispatch<RootState, unknown, UnknownAction>) => {
    dispatch(fetchUserProfileRequest());
    try {
      const res = await api.get("/api/v1/users/profile", {
        headers: {
          Authorization: `Bearer ${jwt}`,
        },
      });
      const { user } = res.data;
      dispatch(fetchUserProfileSuccess(user));
    } catch (error: any) {
      dispatch(
        fetchUserProfileFailure(
          error.response?.data?.validationErrors || "Fetching profile failed"
        )
      );
    }
  };

export const checkAuth =
  (): ThunkAction<void, RootState, unknown, UnknownAction> =>
  async (dispatch) => {
    try {
      const res = await api.get("/api/v1/auth/check");
      const { user, expiresIn } = res.data;
      const tokenExpiresAt = Date.now() + expiresIn * 1000;
      dispatch(signInSuccess({ user, tokenExpiresAt }));
    } catch (error) {
      console.log(error)
      dispatch(signOutSuccess());
    }
  };

export const signOut =
  (): ThunkAction<void, RootState, unknown, UnknownAction> =>
  async (dispatch) => {
    try {
      await api.post("/api/v1/auth/logout");
    } catch (err) {
      console.error("Logout error", err);
    } finally {
      dispatch(signOutSuccess());
    }
  };
