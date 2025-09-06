import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { User } from "../types/userTypes";
import { api } from "../config/api";

export interface AuthState {
  user: User | null;
  isLoggedIn: boolean;
  sentCode: boolean;
  loading: boolean;
  error: Record<string, string> | null;
  isVerified: boolean;
  resetPasswordEmailSent: boolean;
  tokenExpiresAt: number | null;
}

const initialState: AuthState = {
  user: localStorage.getItem("user")
    ? JSON.parse(localStorage.getItem("user")!)
    : null,
  isLoggedIn: localStorage.getItem("user") ? true : false,
  sentCode: false,
  loading: false,
  error: null,
  isVerified: false,
  resetPasswordEmailSent: false,
  tokenExpiresAt: localStorage.getItem("tokenExpiresAt")
    ? Number(localStorage.getItem("tokenExpiresAt"))
    : null,
};

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    signUpRequest(state) {
      state.loading = true;
      state.error = null;
    },
    signUpSuccess(state) {
      state.loading = false;
    },
    signUpFailure(state, action: PayloadAction<Record<string, string>>) {
      state.loading = false;
      state.error = action.payload;
    },
    sendRegisterCodeRequest(state) {
      state.loading = true;
      state.error = null;
    },
    sendRegisterCodeSuccess(state) {
      state.loading = false;
      state.sentCode = true;
    },
    sendRegisterCodeFailure(
      state,
      action: PayloadAction<Record<string, string>>
    ) {
      state.loading = false;
      state.error = action.payload;
    },
    verifyAccountRequest(state) {
      state.loading = true;
      state.error = null;
    },
    verifyAccountSuccess(
      state,
      action: PayloadAction<{
        user?: User;
      }>
    ) {
      state.loading = false;
      state.isVerified = true;
      state.isLoggedIn = true;

      if (action.payload.user) {
        state.user = action.payload.user;
      }
    },
    verifyAccountFailure(state, action: PayloadAction<Record<string, string>>) {
      state.loading = false;
      state.error = action.payload;
    },
    signInRequest(state) {
      state.loading = true;
      state.error = null;
    },
    signInSuccess(
      state,
      action: PayloadAction<{
        user: any;
        tokenExpiresAt: number;
      }>
    ) {
      state.user = action.payload.user;
      state.isLoggedIn = true;
      state.loading = false;
      state.isVerified = true;
      state.tokenExpiresAt = action.payload.tokenExpiresAt;
      localStorage.setItem("user", JSON.stringify(action.payload.user));
      localStorage.setItem(
        "tokenExpiresAt",
        action.payload.tokenExpiresAt.toString()
      );
    },
    signInFailure(state, action: PayloadAction<Record<string, string>>) {
      state.loading = false;
      state.error = action.payload;
    },
    fetchUserProfileRequest(state) {
      state.loading = true;
      state.error = null;
    },
    fetchUserProfileSuccess(state, action: PayloadAction<User>) {
      state.user = action.payload;
      state.loading = false;
      state.isLoggedIn = true;
    },
    fetchUserProfileFailure(
      state,
      action: PayloadAction<Record<string, string>>
    ) {
      state.loading = false;
      state.error = action.payload;
    },
    signOut(state) {
      state.user = null;
      state.isLoggedIn = false;
      state.tokenExpiresAt = null;
      localStorage.removeItem("user");
      localStorage.removeItem("tokenExpiresAt");
      api.defaults.headers.common["Authorization"] = "";
    },
    signOutSuccess(state) {
      state.user = null;
      state.isLoggedIn = false;
      state.tokenExpiresAt = null;
      localStorage.removeItem("user");
      localStorage.removeItem("tokenExpiresAt");
      api.defaults.headers.common["Authorization"] = "";
    },
    setTokenExpiresAt(state, action: PayloadAction<number>) {
      state.tokenExpiresAt = action.payload;
      localStorage.setItem("tokenExpiresAt", action.payload.toString());
    },
    clearError(state) {
      state.error = null;
    },
    forgotPasswordRequest(state) {
      state.loading = true;
      state.error = null;
      state.resetPasswordEmailSent = false;
    },
    forgotPasswordSuccess(state) {
      state.loading = false;
      state.resetPasswordEmailSent = true;
    },
    forgotPasswordFailure(
      state,
      action: PayloadAction<Record<string, string>>
    ) {
      state.loading = false;
      state.error = action.payload;
    },
    resetPasswordRequest(state) {
      state.loading = true;
      state.error = null;
    },
    resetPasswordSuccess(state) {
      state.loading = false;
    },
    resetPasswordFailure(state, action: PayloadAction<Record<string, string>>) {
      state.loading = false;
      state.error = action.payload;
    },
  },
});

export const {
  signUpRequest,
  signUpSuccess,
  signUpFailure,
  sendRegisterCodeRequest,
  sendRegisterCodeSuccess,
  sendRegisterCodeFailure,
  verifyAccountRequest,
  verifyAccountSuccess,
  verifyAccountFailure,
  signInRequest,
  signInSuccess,
  signInFailure,
  fetchUserProfileRequest,
  fetchUserProfileSuccess,
  fetchUserProfileFailure,
  signOut,
  signOutSuccess,
  setTokenExpiresAt,
  clearError,
  forgotPasswordRequest,
  forgotPasswordSuccess,
  forgotPasswordFailure,
  resetPasswordRequest,
  resetPasswordSuccess,
  resetPasswordFailure,
} = authSlice.actions;

export default authSlice.reducer;
