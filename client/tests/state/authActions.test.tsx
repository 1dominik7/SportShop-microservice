import { describe, it, expect, vi, beforeEach } from "vitest";
import {
  fetchUserProfile,
  forgotPassword,
  resetPassword,
  signIn,
  signUp,
  verifyAccount,
} from "../../src/state/authActions";
import { api } from "../../src/config/api";

vi.mock("../../src/config/api", () => ({
  api: {
    post: vi.fn(),
    get: vi.fn(),
  },
}));

describe("authActions", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("should dispatch successful register", async () => {
    const mockDispatch = vi.fn();
    const mockNavigate = vi.fn();

    const payload = {
      email: "test@example.com",
      firstname: "test",
      lastname: "xyz",
      password: "password123",
    };

    (api.post as any).mockResolvedValueOnce({});

    const thunk = signUp(payload, mockNavigate);
    await thunk(mockDispatch);

    expect(mockDispatch).toHaveBeenCalledWith({ type: "auth/signUpRequest" });
    expect(api.post).toHaveBeenCalledWith("/api/v1/auth/register", payload);

    expect(mockDispatch).toHaveBeenCalledWith({ type: "auth/signUpSuccess" });
    expect(mockNavigate).toHaveBeenCalledWith("/verify-account", {
      state: { email: payload.email },
    });
  });

  it("should dispatch successful verification code", async () => {
    const mockDispatch = vi.fn();
    const mockNavigate = vi.fn();

    const tokenCode = "234567";

    (api.get as any).mockResolvedValueOnce({
      data: { token: "token123" },
    });

    const thunk = verifyAccount(tokenCode, mockNavigate);
    await thunk(mockDispatch);

    expect(mockDispatch).toHaveBeenCalledWith({
      type: "auth/verifyAccountRequest",
    });
    expect(api.get).toHaveBeenCalledWith(
      `/api/v1/auth/activate-account?token=${tokenCode}`
    );

    expect(mockDispatch).toHaveBeenCalledWith({
      type: "auth/verifyAccountSuccess",
      payload: { jwt: "token123" },
    });
    expect(mockNavigate).toHaveBeenCalledWith("/");
  });

  it("should dispatch successful login", async () => {
    const mockDispatch = vi.fn();
    const mockNavigate = vi.fn();

    const payload = {
      email: "test@example.com",
      password: "password123",
    };

    const mockResponse = {
      data: {
        token: "token123",
        user: { id: 1, name: "test" },
      },
    };

    (api.post as any).mockResolvedValueOnce(mockResponse);

    await signIn(payload, mockNavigate)(mockDispatch);

    expect(api.post).toHaveBeenCalledWith("/api/v1/auth/authenticate", {
      email: "test@example.com",
      password: "password123",
    });

    expect(mockDispatch).toHaveBeenCalledWith({ type: "auth/signInRequest" });

    expect(mockDispatch).toHaveBeenCalledWith({
      type: "auth/signInSuccess",
      payload: {
        jwt: "token123",
        user: { id: 1, name: "test" },
      },
    });

    expect(mockNavigate).toHaveBeenCalledWith("/");
  });

  it("should dispatch forgot password", async () => {
    const mockDispatch = vi.fn();
    const mockNavigate = vi.fn();
    const email = "test@example.com";

    (api.post as any).mockResolvedValueOnce({
      data: {
        token: "token123",
      },
    });

    const thunk = forgotPassword(email, mockNavigate);
    await thunk(mockDispatch);

    expect(mockDispatch).toHaveBeenCalledWith({
      type: "auth/forgotPasswordRequest",
    });
    expect(api.post).toHaveBeenCalledWith("/api/v1/auth/forgot-password", {
      email: email,
    });

    expect(mockDispatch).toHaveBeenCalledWith({
      type: "auth/forgotPasswordSuccess",
    });

    expect(mockNavigate).toHaveBeenCalledWith("/reset-password/token123", {
      state: { email },
    });
  });

  it("should dispatch reset password", async () => {
    const mockDispatch = vi.fn();
    const mockNavigate = vi.fn();

    const payload = {
      token: "token123",
      newPassword: "password123",
      confirmPassword: "password123",
    };

    (api.post as any).mockResolvedValueOnce({});

    const thunk = resetPassword(payload, mockNavigate);
    await thunk(mockDispatch);

    expect(mockDispatch).toHaveBeenCalledWith({
      type: "auth/resetPasswordRequest",
    });

    expect(api.post).toHaveBeenCalledWith("/api/v1/auth/reset-password", {
      token: "token123",
      newPassword: "password123",
      confirmPassword: "password123",
    });

    expect(mockDispatch).toHaveBeenCalledWith({
      type: "auth/resetPasswordSuccess",
    });

    expect(mockNavigate).toHaveBeenCalledWith("/login");
  });

  it("should dispatch fetch user profile", async () => {
    const mockDispatch = vi.fn();
    const jwt = "jwt-fake-token";

    const mockUser = {
      id: 1,
      firstname: "test",
      lastname: "xyz",
      email: "text@example.pl",
    };

    (api.get as any).mockResolvedValueOnce({
      data: {
        user: mockUser,
      },
    });

    const thunk = fetchUserProfile(jwt);
    await thunk(mockDispatch);

    expect(mockDispatch).toHaveBeenCalledWith({
      type: "auth/fetchUserProfileRequest",
    });

    expect(api.get).toHaveBeenCalledWith("/api/v1/users/profile", {
      headers: {
        Authorization: `Bearer ${jwt}`,
      },
    });

    expect(mockDispatch).toHaveBeenCalledWith({
      type: "auth/fetchUserProfileSuccess",
      payload: mockUser,
    });
  });
});
