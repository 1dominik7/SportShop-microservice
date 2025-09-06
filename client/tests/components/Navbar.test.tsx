import React from "react";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import Navbar from "../../src/components/Navbar";
import { Provider } from "react-redux";
import { configureStore } from "@reduxjs/toolkit";
import authReducer from "../../src/state/authSlice";
import { MemoryRouter } from "react-router-dom";
import type { AuthState } from "../../src/state/authSlice";
import * as hooks from "../../src/hooks/query";

const getMockAuthState = (overrides?: Partial<AuthState>): AuthState => {
  const { tokenExpiresAt = null, ...restOverrides } = overrides || {};

  return {
    user: null,
    isLoggedIn: false,
    sentCode: false,
    loading: false,
    error: null,
    isVerified: false,
    resetPasswordEmailSent: false,
    tokenExpiresAt,
    ...restOverrides,
  };
};

const mockNavigate = vi.fn();

vi.mock("react-router", async () => {
  const actual = await vi.importActual("react-router");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
    useLocation: () => ({
      pathname: "/",
      search: "",
      hash: "",
      state: null,
      key: "default",
    }),
  };
});

vi.mock("../../src/hooks/query", async () => {
  const actual = await vi.importActual("../../src/hooks/query");
  return {
    ...actual,
    useCategoryQuery: () => ({
      data: [
        {
          id: 1,
          categoryName: "Boots",
          parentCategoryId: null,
          variations: [
            {
              id: 1,
              name: "Purpose",
              options: [
                { id: 1, value: "Grass" },
                { id: 2, value: "Indoor Shoes" },
              ],
              categoryId: 1,
            },
            {
              id: 2,
              name: "Advancement",
              options: [
                { id: 3, value: "Recreational" },
                { id: 4, value: "Professional" },
              ],
              categoryId: 1,
            },
          ],
        },
        {
          id: 2,
          categoryName: "Fan Clothing",
          parentCategoryId: null,
          variations: [],
        },
      ],
      isLoading: false,
      isError: false,
    }),
    useCartByUserId: () => ({ data: null }),
    useFavorites: () => ({ data: [] }),
  };
});

const renderWithStore = (authStateOverride?: Partial<AuthState>) => {
  const store = configureStore({
    reducer: {
      auth: authReducer,
    },
    preloadedState: {
      auth: getMockAuthState(authStateOverride),
    },
  });

  return render(
    <Provider store={store}>
      <MemoryRouter>
        <Navbar />
      </MemoryRouter>
    </Provider>
  );
};

describe("Navbar", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  vi.spyOn(hooks, "useCategoryQuery").mockReturnValue({
    data: [
      {
        id: 1,
        categoryName: "Boots",
        parentCategoryId: null,
        variations: [
          {
            id: 1,
            name: "Purpose",
            options: [
              { id: 1, value: "Grass" },
              { id: 2, value: "Indoor Shoes" },
            ],
            categoryId: 1,
          },
        ],
      },
    ],
    isLoading: false,
    isError: false,
    error: null,
  });

  it("logout should not be displayed when user is not logged in", () => {
    renderWithStore({ isLoggedIn: false });

    const logoutText = screen.queryByText(/logout/i);
    expect(logoutText).not.toBeInTheDocument();
  });

  it("logout should be displayed when user is logged in", () => {
    renderWithStore({ isLoggedIn: true });

    const logoutText = screen.queryByText(/logout/i);
    expect(logoutText).toBeInTheDocument();
  });

  it("should not display favorite count when there are no favorites", () => {
    vi.spyOn(hooks, "useFavorites").mockReturnValue({
      data: [],
      isSuccess: true,
      refetch: vi.fn(),
    } as any);

    renderWithStore({ isLoggedIn: true });

    const badge = screen.queryByText("0");
    expect(badge).not.toBeInTheDocument();
  });

  it("should display favorite count when there are favorites", () => {
    vi.spyOn(hooks, "useFavorites").mockReturnValue({
      data: [1, 2, 3],
      isSuccess: true,
      refetch: vi.fn(),
    } as any);

    renderWithStore({ isLoggedIn: true });

    const badge = screen.queryByText("3");
    expect(badge).toBeInTheDocument();
  });

  it("should render categories from useCategoryQuery", () => {
    vi.spyOn(hooks, "useCategoryQuery").mockReturnValue({
      data: [
        {
          id: 1,
          categoryName: "Shoes",
          parentCategoryId: null,
          variations: [],
        },
        {
          id: 2,
          categoryName: "Fan Clothing",
          parentCategoryId: null,
          variations: [],
        },
      ],
      isLoading: false,
      isError: false,
      error: null,
    });

    renderWithStore();

    const shoesCategory = screen.getByText(/Shoes/i);
    const fanClothingCategory = screen.getByText(/Fan Clothing/i);

    expect(shoesCategory).toBeInTheDocument();
    expect(fanClothingCategory).toBeInTheDocument();
  });

  it("should navigate to correct category on category click", () => {
    vi.spyOn(hooks, "useCategoryQuery").mockReturnValue({
      data: [
        {
          id: 1,
          categoryName: "Shoes",
          parentCategoryId: null,
          variations: [],
        },
        {
          id: 2,
          categoryName: "Fan Clothing",
          parentCategoryId: null,
          variations: [],
        },
      ],
      isLoading: false,
      isError: false,
      error: null,
    });

    renderWithStore();

    const categoryButton = screen.getByText(/Fan Clothing/i);

    fireEvent.click(categoryButton);

    expect(mockNavigate).toHaveBeenCalledWith("/products?category=2", {
      replace: true,
    });
  });

  it("should display filters clicking in variation", async () => {
    vi.spyOn(hooks, "useCategoryQuery").mockReturnValue({
      data: [
        {
          id: 1,
          categoryName: "Boots",
          parentCategoryId: null,
          variations: [
            {
              id: 1,
              name: "Purpose",
              options: [
                { id: 1, value: "Grass" },
                { id: 2, value: "Indoor Shoes" },
              ],
              categoryId: 1,
            },
          ],
        },
      ],
      isLoading: false,
      isError: false,
      error: null,
    });

    renderWithStore();

    const categoryButton = screen.getByText(/Boots/i);
    fireEvent.mouseEnter(categoryButton);

    const filterVariation = await screen.findByText(/Purpose/i);
    fireEvent.click(filterVariation);

    expect(mockNavigate).toHaveBeenCalledWith(
      "/products?category=1&filters=1%5B1%252%5D"
    );
  });

  it("should display filters clicking in variationOption", async () => {
    vi.spyOn(hooks, "useCategoryQuery").mockReturnValue({
      data: [
        {
          id: 1,
          categoryName: "Boots",
          parentCategoryId: null,
          variations: [
            {
              id: 1,
              name: "Purpose",
              options: [
                { id: 1, value: "Grass" },
                { id: 2, value: "Indoor Shoes" },
              ],
              categoryId: 1,
            },
          ],
        },
      ],
      isLoading: false,
      isError: false,
      error: null,
    });

    renderWithStore();

    const categoryButton = await screen.findByText(/Boots/i);
    fireEvent.mouseEnter(categoryButton);

    await waitFor(() => {
      expect(screen.getByText(/Grass/i)).toBeInTheDocument();
    });

    const filterLink = await screen.findByRole("link", { name: /Grass/i });

    expect(filterLink).toHaveAttribute(
      "href",
      "/products?category=1&filters=1[1]"
    );

    fireEvent.click(filterLink);
  });
});
