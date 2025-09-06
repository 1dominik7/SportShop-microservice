import React, { act, createRef } from "react";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import ProductItemRight from "../../src/components/productItemPage/ProductItemRight";
import {
  ProductItemByColour,
  ProductItemOneByColour,
  UserReviewProductById,
} from "../../src/types/userTypes";
import { configureStore } from "@reduxjs/toolkit";
import { AuthState } from "../../src/state/authSlice";
import authReducer from "../../src/state/authSlice";
import { Provider } from "react-redux";
import { MemoryRouter } from "react-router";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";

const mockNavigate = vi.fn();
const mockFavorites = [1, 2, 3];
const mockToggleFavorites = vi.fn();
const getFavoritesFromCookies = vi.fn();
const setFavoritesInCookies = vi.fn();
const mockMutateAsync = vi.fn().mockResolvedValue({});

vi.mock("react-router", async () => {
  const actual = await vi.importActual("react-router");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

vi.mock("../../src/hooks/query", () => ({
  useFavorites: vi.fn(() => ({ data: mockFavorites, isSuccess: true })),
  useToggleFavorite: vi.fn(() => ({
    mutate: mockToggleFavorites,
  })),
  mutation: {
    mutateAsync: vi.fn(),
  },
}));

vi.mock("@tanstack/react-query", async () => {
  const actual = await vi.importActual("@tanstack/react-query");
  return {
    ...actual,
    useMutation: () => ({
      mutateAsync: mockMutateAsync,
      isLoading: false,
    }),
  };
});

vi.mock("react-toastify", async () => {
  const actual = await vi.importActual("react-toastify");
  return {
    ...actual,
    toast: {
      error: vi.fn(),
    },
    ToastContainer: actual.ToastContainer,
  };
});

const queryClient = new QueryClient();

const mockProductItemByColour: ProductItemByColour = {
  id: 1,
  productId: 1,
  productItemId: 1,
  productName: "Korki Nike Mercurial Superfly 10 Club FG/MG",
  colour: "red",
  productImages: [
    {
      id: 1,
      imageFilename:
        "https://res.cloudinary.com/dominikdev/image/upload/v1744983591/ecommerce/xuomesdhkzaeoyidroaq.webp",
    },
    {
      id: 2,
      imageFilename:
        "https://res.cloudinary.com/dominikdev/image/upload/v1744983591/ecommerce/fqh6ozrfzl4qyh83pyxx.webp",
    },
  ],
  productItemOneByColour: [
    {
      id: 2,
      categoryId: 1,
      productId: 1,
      colour: "red",
      discount: 0,
      price: 55,
      productCode: "qwe",
      productDescription: "Super lekkie korki do gry na trawie",
      productImages: [
        {
          id: 1,
          imageFilename:
            "https://res.cloudinary.com/dominikdev/image/upload/v1744983591/ecommerce/xuomesdhkzaeoyidroaq.webp",
        },
      ],
      productName: "Korki Nike Mercurial Superfly 10 Club FG/MG",
      qtyInStock: 5,
      variations: [
        {
          id: 1,
          categoryId: 1,
          name: "size",
          options: [
            {
              id: 8,
              value: "40",
            },
          ],
        },
        {
          id: 2,
          categoryId: 1,
          name: "colour",
          options: [
            {
              id: 6,
              value: "red",
            },
          ],
        },
      ],
    },
  ],
  otherProductItemOneByColours: [],
};

const mockReview: UserReviewProductById = {
  productId: 1,
  reviews: [],
  averageRating: 0.0,
  totalReviews: 0,
};

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

const dummyRef = createRef<HTMLDivElement>();
const mockSetOpenSection = vi.fn();

const renderWithStore = (
  authStateOverride?: Partial<AuthState>,
  initialFavorites: number[] = []
) => {
  getFavoritesFromCookies.mockReturnValue(initialFavorites);

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
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <ProductItemRight
            data={mockProductItemByColour}
            review={mockReview}
            productDescRef={dummyRef}
            productDetailsRef={dummyRef}
            productReviewsRef={dummyRef}
            deliveryRef={dummyRef}
            openSection={{ 1: true }}
            setOpenSection={mockSetOpenSection}
          />
        </MemoryRouter>
      </QueryClientProvider>
    </Provider>
  );
};

describe("ProductItem", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("should show error if productItem is not selected", () => {
    renderWithStore({ isLoggedIn: false });

    const button = screen.getByText("Add to cart");
    fireEvent.click(button);

    expect(screen.getByText("Select Size")).toBeInTheDocument();
  });

  it("should navigate to login if user is not logout", async () => {
    renderWithStore({ isLoggedIn: false });

    fireEvent.click(screen.getByText(/40/));

    const button = screen.getByText("Add to cart");
    await act(async () => {
      fireEvent.click(button);
    });
    console.log("mockNavigate calls:", mockNavigate.mock.calls);
    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith("/login");
    });
  });

  it("should toggle favorite and call toggle function when clicked", async () => {
    const mockProductItemId = 2;

    getFavoritesFromCookies.mockReturnValue([mockProductItemId]);

    mockToggleFavorites.mockImplementation((id) => {
      setFavoritesInCookies([]);
      return Promise.resolve([]);
    });

    renderWithStore({ isLoggedIn: true }, [mockProductItemId]);

    fireEvent.click(screen.getByText(/40/));

    const toggleDiv = screen.getByTestId("favorite-toggle");
    fireEvent.click(toggleDiv);

    await waitFor(() => {
      expect(mockToggleFavorites).toHaveBeenCalledWith(mockProductItemId);
      expect(setFavoritesInCookies).toHaveBeenCalledWith([]);
    });

    mockToggleFavorites.mockImplementation((id) => {
      setFavoritesInCookies([mockProductItemId]);
      return Promise.resolve([mockProductItemId]);
    });

    fireEvent.click(toggleDiv);

    await waitFor(() => {
      expect(mockToggleFavorites).toHaveBeenCalledWith(mockProductItemId);
      expect(setFavoritesInCookies).toHaveBeenCalledWith([mockProductItemId]);
    });
  });

  it("should add to cart product item", async () => {
    renderWithStore({
      isLoggedIn: true,
      user: {
        id: 1,
        email: "test@example.pl",
        fullName: "test xyz",
        accountName: true,
        dateOfBirth: "",
        enabled: true,
        roleNames: ["admin"],
      },
    });

    fireEvent.click(screen.getByText(/40/));

    const button = screen.getByText("Add to cart");
    fireEvent.click(button);

    await waitFor(() => {
      expect(mockMutateAsync).toHaveBeenCalledWith({
        productItemId: mockProductItemByColour.productItemOneByColour[0].id,
        quantity: 1,
      });
    });
  });

  it("should render edit Button when user is admin", () => {
    renderWithStore({
      isLoggedIn: true,
      user: {
        id: 1,
        email: "test@example.pl",
        fullName: "test xyz",
        accountName: true,
        dateOfBirth: "",
        enabled: true,
        roleNames: ["admin"],
      },
    });

    expect(
      screen.getByRole("button", { name: "Edit Product" })
    ).toBeInTheDocument();
  });

  it("should not render edit Button when user is admin", () => {
    renderWithStore({
      isLoggedIn: true,
      user: {
        id: 1,
        email: "test@example.pl",
        fullName: "test xyz",
        accountName: true,
        dateOfBirth: "",
        enabled: true,
        roleNames: ["user"],
      },
    });

    expect(
      screen.queryByRole("button", { name: "Edit Product" })
    ).not.toBeInTheDocument();
  });
});
