import React from "react";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import Login from "../../src/pages/auth/Login";
import { Provider } from "react-redux";
import { configureStore } from "@reduxjs/toolkit";
import authReducer from "../../src/state/authSlice";
import { MemoryRouter } from "react-router-dom";

const mockNavigate = vi.fn();

vi.mock("react-router", async () => {
  const actual = await vi.importActual("react-router");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

vi.mock("../../src/state/authActions", () => ({
  signIn: vi.fn(() => ({ type: "MOCK_SIGN_IN" })),
}));

vi.mock("../../src/config/api", () => ({
  api: {
    post: vi.fn(),
  },
}));

describe("Login", () => {
  let store: ReturnType<typeof configureStore>;

  beforeEach(() => {
    store = configureStore({
      reducer: {
        auth: authReducer,
      },
    });
    vi.clearAllMocks();
  });

  it("should render the login form with fields and button", () => {
    render(
      <Provider store={store}>
        <MemoryRouter>
          <Login />
        </MemoryRouter>
      </Provider>
    );

    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: /sign in/i })
    ).toBeInTheDocument();
  });

  it("should validate the form", async () => {
    render(
      <Provider store={store}>
        <MemoryRouter>
          <Login />
        </MemoryRouter>
      </Provider>
    );

    fireEvent.click(screen.getByRole("button", { name: /sign in/i }));

    await waitFor(async () => {
      expect(
        await screen.findByText(/email is mandatory/i)
      ).toBeInTheDocument();
      expect(
        await screen.findByText(/password is mandatory/i)
      ).toBeInTheDocument();
    });
  });

  it("text field in Login should work", () => {
    render(
      <Provider store={store}>
        <MemoryRouter>
          <Login />
        </MemoryRouter>
      </Provider>
    );

    const emailInput = screen.getByLabelText("Email") as HTMLInputElement;
    const passwordInput = screen.getByLabelText("Password") as HTMLInputElement;

    fireEvent.change(emailInput, { target: { value: "test@example.com" } });
    fireEvent.change(passwordInput, { target: { value: "password123" } });

    expect(emailInput.value).toBe("test@example.com");
    expect(passwordInput.value).toBe("password123");
  });

  it("submits form and dispatches signIn", async () => {
    render(
      <Provider store={store}>
        <MemoryRouter>
          <Login />
        </MemoryRouter>
      </Provider>
    );

    const emailInput = screen.getByLabelText("Email");
    const passwordInput = screen.getByLabelText("Password");
    const submitButton = screen.getByRole("button", { name: /sign in/i });

    fireEvent.change(emailInput, { target: { value: "test@example.com" } });
    fireEvent.change(passwordInput, { target: { value: "password123" } });

    fireEvent.click(submitButton);

    await waitFor(async () => {
      const { signIn } = await vi.importMock("../../src/state/authActions");
      expect(signIn).toHaveBeenCalledWith(
        {
          email: "test@example.com",
          password: "password123",
        },
        mockNavigate
      );
    });
  });
});
