import React from "react";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import Register from "../../src/pages/auth/Register";
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
  signUp: vi.fn(() => ({ type: "MOCK_SIGN_UP" })),
  __esModule: true,
}));

describe("Register", () => {
  let store: ReturnType<typeof configureStore>;

  beforeEach(() => {
    store = configureStore({
      reducer: {
        auth: authReducer,
      },
    });
    vi.clearAllMocks();
  });

  it("text field in Register should work", () => {
    render(
      <Provider store={store}>
        <MemoryRouter>
          <Register />
        </MemoryRouter>
      </Provider>
    );

    const emailInput = screen.getByLabelText("Email") as HTMLInputElement;
    const firstNameInput = screen.getByLabelText(
      "First Name"
    ) as HTMLInputElement;
    const lastNameInput = screen.getByLabelText(
      "Last Name"
    ) as HTMLInputElement;
    const passwordInput = screen.getByLabelText("Password") as HTMLInputElement;

    fireEvent.change(emailInput, { target: { value: "test@example.com" } });
    fireEvent.change(firstNameInput, { target: { value: "test" } });
    fireEvent.change(lastNameInput, { target: { value: "xyz" } });
    fireEvent.change(passwordInput, { target: { value: "password123" } });

    expect(emailInput.value).toBe("test@example.com");
    expect(firstNameInput.value).toBe("test");
    expect(lastNameInput.value).toBe("xyz");
    expect(passwordInput.value).toBe("password123");
  });

  it("should render the register form with fields and button", () => {
    render(
      <Provider store={store}>
        <MemoryRouter>
          <Register />
        </MemoryRouter>
      </Provider>
    );

    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/first name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/last name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: /sign up/i })
    ).toBeInTheDocument();
  });

  it("should validate the register form", async () => {
    render(
      <Provider store={store}>
        <MemoryRouter>
          <Register />
        </MemoryRouter>
      </Provider>
    );

    fireEvent.click(screen.getByRole("button", { name: /sign up/i }));

    await waitFor(async () => {
      expect(
        await screen.findByText(/email is mandatory/i)
      ).toBeInTheDocument();
      expect(
        await screen.findByText(/firstname is mandatory/i)
      ).toBeInTheDocument();
      expect(
        await screen.findByText(/lastname is mandatory/i)
      ).toBeInTheDocument();
      expect(
        await screen.findByText(/password is mandatory/i)
      ).toBeInTheDocument();
    });
  });

  it("submits form and dispatches signUp", async () => {
    render(
      <Provider store={store}>
        <MemoryRouter>
          <Register />
        </MemoryRouter>
      </Provider>
    );

    const emailInput = screen.getByLabelText("Email") as HTMLInputElement;
    const firstNameInput = screen.getByLabelText(
      "First Name"
    ) as HTMLInputElement;
    const lastNameInput = screen.getByLabelText(
      "Last Name"
    ) as HTMLInputElement;
    const passwordInput = screen.getByLabelText("Password") as HTMLInputElement;
    const submitButton = screen.getByRole("button", { name: /sign up/i });

    fireEvent.change(emailInput, { target: { value: "test@example.com" } });
    fireEvent.change(firstNameInput, { target: { value: "test" } });
    fireEvent.change(lastNameInput, { target: { value: "xyz" } });
    fireEvent.change(passwordInput, { target: { value: "password123" } });

    expect(emailInput.value).toBe("test@example.com");
    expect(firstNameInput.value).toBe("test");
    expect(lastNameInput.value).toBe("xyz");
    expect(passwordInput.value).toBe("password123");

    fireEvent.click(submitButton);

    await waitFor(async () => {
      const { signUp } = await vi.importMock("../../src/state/authActions");
      expect(signUp).toHaveBeenCalledWith(
        {
          email: "test@example.com",
          firstname: "test",
          lastname: "xyz",
          password: "password123",
        },
        mockNavigate
      );
    });
  });
});
