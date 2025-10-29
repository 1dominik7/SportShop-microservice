import {
  keepPreviousData,
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import {
  Address,
  CartItem,
  Category,
  FamousShoes,
  Filter,
  GetShopOrder,
  LatestSalesProductsResponse,
  OrderList,
  Product,
  ProductItem,
  ProductItemByColour,
  ProductItemList,
  ProductItemListGroupedByFilters,
  ProductItemsFilters,
  ShopOrderStatistics,
  Statistics,
  TopProductSales,
  UserList,
} from "../types/userTypes";
import { api } from "../config/api";
import { useSelector } from "react-redux";
import { RootState } from "../state/store";

export const fetchCategoriesById = async (
  categoryId: number[]
): Promise<Category[]> => {
  const res = await api.get(`/api/v1/category/byId?ids=${categoryId}`);
  return res.data;
};

export const useCategoryQuery = (categoryId: number[]) => {
  const { data, error, isLoading, isError } = useQuery<Category[], Error>({
    queryKey: ["category", categoryId],
    queryFn: () => fetchCategoriesById(categoryId),
  });

  if (isError && error) {
    console.error("Error fetching categories: ", error);
  }

  return { data, error, isLoading, isError };
};

export const fetchNewestProducts = async (
  numberOfProducts: number
): Promise<ProductItem[]> => {
  const res = await api.get(
    `/api/v1/productItems/getAll?items=${numberOfProducts}`
  );
  return res.data;
};

export const useNewestProductsQuery = (numberOfProducts: number) => {
  const { data, error, isLoading, isError } = useQuery<ProductItem[], Error>({
    queryKey: ["newestProducts", numberOfProducts],
    queryFn: () => fetchNewestProducts(numberOfProducts),
  });

  if (isError && error) {
    console.error("Error fetching newest products: ", error);
  }

  return { data, error, isLoading, isError };
};

export const fetchFamousShoesCollection = async (
  variationId: number,
  variationOptionId: number
): Promise<FamousShoes[]> => {
  const res = await api.get(
    `/api/v1/products/searchByCategory?variationId=${variationId}&variationOptionId=${variationOptionId}&pageNumber=0&pageSize=10&sortBy=id&sortOrder=asc`
  );
  return res.data;
};

export const useFamousShoesCollectionQuery = (
  variationId: number,
  variationOptionId: number
) => {
  const { data, error, isLoading, isError } = useQuery<FamousShoes[], Error>({
    queryKey: ["famousShoes", variationId, variationOptionId],
    queryFn: () => fetchFamousShoesCollection(variationId, variationOptionId),
  });

  if (isError && error) {
    console.error("Error fetching newest products: ", error);
  }

  return { data, error, isLoading, isError };
};

export const fetchFilters = async (categoryId: number): Promise<Filter[]> => {
  const res = await api.get(`/api/v1/category/byId?ids=${categoryId}`);
  return res.data;
};

export const useFetchFilters = (categoryId: number) => {
  const { data, error, isLoading, isError } = useQuery<Filter[], Error>({
    queryKey: ["filters", categoryId],
    queryFn: () => fetchFilters(categoryId),
  });

  if (isError && error) {
    console.error("Error fetching filters: ", error);
  }

  return { data, error, isLoading, isError };
};

export const buildVariationIds = (selectedOption: {
  [key: number]: { optionId: number[] };
}) => {
  return Object.keys(selectedOption).join(",");
};

export const buildVariationOptionIds = (selectedOption: {
  [key: number]: { optionId: number[] };
}) => {
  const variationOptionIds: number[] = [];
  Object.values(selectedOption).forEach(({ optionId }) => {
    variationOptionIds.push(...optionId);
  });
  return variationOptionIds.join(",");
};

export const fetchProductsByFilters = async (
  categoryId: number,
  selectedOption: { [key: number]: { optionId: number[] } },
  pageNumber: number = 0,
  pageSize: number = 20,
  sortBy: string = "id",
  sortOrder: string = "asc"
): Promise<ProductItemList> => {
  const variationIds = buildVariationIds(selectedOption);
  const variationOptionIds = buildVariationOptionIds(selectedOption);
  const url = `/api/v1/productItems/searchByCategory?categoryId=${categoryId}&variationIds=${variationIds}&variationOptionIds=${variationOptionIds}&pageNumber=${pageNumber}&pageSize=${pageSize}&sortBy=${sortBy}&sortOrder=${sortOrder}`;

  const res = await api.get(url);
  return res.data;
};

export const useProductsByFilters = (
  categoryId: number,
  selectedOption: { [key: number]: { optionId: number[] } }
) => {
  const { data, error, isFetching, isLoading, isError, refetch } = useQuery<
    ProductItemList,
    Error
  >({
    queryKey: ["productsByCategory", categoryId, selectedOption],
    queryFn: () => fetchProductsByFilters(categoryId, selectedOption),
  });

  if (isError && error) {
    console.error("Error fetching products by category: ", error);
  }

  return { data, error, isFetching, isLoading, isError, refetch };
};

export const fetchProductItemById = async (
  productItemId: number,
  colour: string
): Promise<ProductItemByColour> => {
  const res = await api.get(
    `/api/v1/productItems/${productItemId}?colour=${colour}`
  );
  return res.data;
};

export const useProductItemById = (productItemId: number, colour: string) => {
  const { data, error, isFetching, isError, isLoading } =
    useQuery<ProductItemByColour>({
      queryKey: ["productItemById", productItemId, colour],
      queryFn: () => fetchProductItemById(productItemId, colour),
    });

  if (isError && error) {
    console.error("Error fetching product item by id: ", error);
  }

  return { data, error, isFetching, isError, isLoading };
};

export const fetchProductItemByProductIdAndColour = async (
  productId: number,
  colour: string
): Promise<ProductItemByColour> => {
  const res = await api.get(`/api/v1/productItems/by-product-id-and-colour`, {
    params: {
      productId,
      colour,
    },
  });
  return res.data;
};

export const useProductItemByProductIdAndColour = (
  productId: number,
  colour: string
) => {
  return useQuery<ProductItemByColour>({
    queryKey: ["productItemByProductIdAndColour", productId, colour],
    queryFn: () => fetchProductItemByProductIdAndColour(productId, colour),
    enabled: !!productId && !!colour,
  });
};

export const fetchProductsByFiltersGrouped = async (
  categoryId?: number,
  selectedOption: { [key: number]: { optionId: number[] } } = {},
  pageNumber: number = 0,
  pageSize: number = 24,
  sortBy: string = "id",
  sortOrder: string = "asc",
  limit?: number
): Promise<ProductItemListGroupedByFilters> => {
  let url = `/api/v1/productItems/searchByColour?pageNumber=${pageNumber}&pageSize=${pageSize}&sortBy=${sortBy}&sortOrder=${sortOrder}`;

  if (limit) {
    url += `&limit=${limit}`;
  }

  if (categoryId) {
    url += `&categoryId=${categoryId}`;
  }

  if (Object.keys(selectedOption).length > 0) {
    const variationOptionIds = buildVariationOptionIds(selectedOption);
    const variationIds = buildVariationIds(selectedOption);
    url += `&variationIds=${variationIds}&variationOptionIds=${variationOptionIds}`;
  }

  const res = await api.get(url);
  return res.data;
};

export const useProductsByFiltersGrouped = (
  categoryId?: number,
  selectedOption: { [key: number]: { optionId: number[] } } = {},
  pageNumber: number = 0,
  pageSize: number = 24,
  sortBy: string = "id",
  sortOrder: string = "asc",
  limit?: number
) => {
  const { data, error, isFetching, isLoading, isError, refetch } = useQuery<
    ProductItemListGroupedByFilters,
    Error,
    ProductItemListGroupedByFilters
  >({
    queryKey: [
      "productsByCategoryGrouped",
      categoryId,
      selectedOption,
      pageNumber,
      pageSize,
      sortBy,
      sortOrder,
      limit,
    ],
    queryFn: () =>
      fetchProductsByFiltersGrouped(
        categoryId,
        selectedOption,
        pageNumber,
        pageSize,
        sortBy,
        sortOrder,
        limit
      ),
    placeholderData: keepPreviousData,
  });

  if (isError && error) {
    console.error(
      "Error fetching products by category and selected Option: ",
      error
    );
  }

  return { data, error, isFetching, isLoading, isError, refetch };
};

export const fetchProductItemsFilters = async (
  categoryId?: number,
  selectedOption: { [key: number]: { optionId: number[] } } = {}
): Promise<ProductItemsFilters[]> => {
  let url = `/api/v1/productItems/filters`;

  if (categoryId) {
    url += `?categoryId=${categoryId}`;
  }

  if (Object.keys(selectedOption).length > 0) {
    const variationOptionIds = buildVariationOptionIds(selectedOption);
    const variationIds = buildVariationIds(selectedOption);
    url += `&variationIds=${variationIds}&variationOptionIds=${variationOptionIds}`;
  }

  const res = await api.get(url);
  return res.data;
};

export const useProductItemsFilters = (
  categoryId?: number,
  selectedOption: { [key: number]: { optionId: number[] } } = {}
) => {
  const { data, error, isFetching, isLoading, isError, refetch } = useQuery<
    ProductItemsFilters[],
    Error
  >({
    queryKey: ["productItemsFilters", categoryId, selectedOption],
    queryFn: () => fetchProductItemsFilters(categoryId, selectedOption),
    placeholderData: keepPreviousData,
  });

  if (isError && error) {
    console.error(
      "Error fetching productItemsFilters category and selected Option: ",
      error
    );
  }

  return { data, error, isFetching, isLoading, isError, refetch };
};

export const fetchProductById = async (productId: number): Promise<Product> => {
  const res = await api.get(`/api/v1/products/${productId}`);
  return res.data;
};

export const useProductById = (productId: number) => {
  const { data, error, isLoading, isFetching, isError, refetch } = useQuery<
    Product,
    Error
  >({
    queryKey: ["productById", productId],
    queryFn: () => fetchProductById(productId),
  });

  if (isError && error) {
    console.error("Error fetching product by id: ", error);
  }

  return { data, error, isLoading, isFetching, isError, refetch };
};

export const fetchCartByUserId = async (): Promise<CartItem> => {
  const res = await api.get(`/api/v1/cart/users/cart`);
  return res.data;
};

export const useCartByUserId = () => {
  const isLoggedIn = useSelector((state: RootState) => !!state.auth.isLoggedIn);
  const { data, error, isLoading, isFetching, isError, refetch } = useQuery<
    CartItem,
    Error
  >({
    queryKey: ["userCart"],
    queryFn: fetchCartByUserId,
    enabled: isLoggedIn,
  });

  if (isError && error) {
    console.error("Error fetching user cart by id: ", error);
  }

  return { data, error, isLoading, isFetching, isError, refetch };
};

export const fetchUserAddresses = async (): Promise<Address[]> => {
  const res = await api.get(`/api/v1/address`);
  return res.data;
};

export const useUserAddresses = () => {
  const { data, error, isLoading, isFetching, isError, refetch } = useQuery<
    Address[],
    Error
  >({
    queryKey: ["userAddresses"],
    queryFn: () => fetchUserAddresses(),
  });

  if (isError && error) {
    console.error("Error fetching user addresses by id: ", error);
  }

  return { data, error, isLoading, isFetching, isError, refetch };
};

export const fetchUserShopOrders = async (): Promise<GetShopOrder[]> => {
  const res = await api.get(`/api/v1/shop-order/user`);
  return res.data;
};

export const useUserShopOrders = () => {
  const { data, error, isLoading, isFetching, isError, refetch } = useQuery<
    GetShopOrder[],
    Error
  >({
    queryKey: ["userShopOrders"],
    queryFn: fetchUserShopOrders,
  });

  if (isError && error) {
    console.error("Error fetching user orders by id: ", error);
  }

  return { data, error, isLoading, isFetching, isError, refetch };
};

export const fetchAllShopOrders = async (
  page: number,
  size: number,
  sortBy: string,
  direction: string,
  query: string,
  searchBy: string
): Promise<OrderList> => {
  const res = await api.get(
    `/api/v1/shop-order/all?page=${page}&size${size}&sortBy=${sortBy}&direction=${direction}&query=${query}&searchBy=${searchBy}`
  );
  return res.data;
};

export const useAllShopOrders = (
  page: number,
  size: number,
  sortBy: string,
  direction: string,
  query: string,
  searchBy: string
) => {
  const { data, error, isLoading, isFetching, isError, refetch } = useQuery<
    OrderList,
    Error
  >({
    queryKey: ["allShopOrders", page, size, sortBy, direction, query, searchBy],
    queryFn: () =>
      fetchAllShopOrders(page, size, sortBy, direction, query, searchBy),
  });

  if (isError && error) {
    console.error("Error fetching all orders: ", error);
  }

  return { data, error, isLoading, isFetching, isError, refetch };
};

export const fetchAllUsers = async (
  page: number,
  size: number,
  sortBy: string,
  direction: string,
  query: string,
  searchBy: string
): Promise<UserList> => {
  const res = await api.get(
    `/api/v1/users/all?page=${page}&size${size}&sortBy=${sortBy}&direction=${direction}&query=${query}&searchBy=${searchBy}`
  );
  return res.data;
};

export const useAllUsers = (
  page: number,
  size: number,
  sortBy: string,
  direction: string,
  query: string,
  searchBy: string
) => {
  const { data, error, isLoading, isFetching, isError, refetch } = useQuery<
    UserList,
    Error
  >({
    queryKey: ["allUsers", page, size, sortBy, direction, query, searchBy],
    queryFn: () =>
      fetchAllUsers(page, size, sortBy, direction, query, searchBy),
  });

  if (isError && error) {
    console.error("Error fetching all users: ", error);
  }

  return { data, error, isLoading, isFetching, isError, refetch };
};

export const getFavoritesFromCookies = (): number[] => {
  const cookies = document.cookie.split(";");
  const favoritesCookie = cookies.find((c) =>
    c.trim().startsWith("favorites=")
  );

  if (favoritesCookie) {
    try {
      const parsed = JSON.parse(favoritesCookie.split("=")[1]);
      return Array.isArray(parsed)
        ? parsed.map(Number).filter((n) => !isNaN(n))
        : [];
    } catch (e) {
      console.error("Error parsing favorites cookie", e);
      return [];
    }
  }
  return [];
};

export const setFavoritesInCookies = (favorites: number[]) => {
  document.cookie = `favorites=${JSON.stringify(favorites)}; path=/; max-age=${
    30 * 24 * 60 * 60
  }`;
};

export const useFavorites = () => {
  return useQuery<number[], Error>({
    queryKey: ["favorites"],
    queryFn: () => Promise.resolve(getFavoritesFromCookies()),
  });
};

export const useToggleFavorite = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (productItemId: number) => {
      const currentFavorites = getFavoritesFromCookies();
      let updatedFavorites: number[];

      if (currentFavorites.includes(productItemId)) {
        updatedFavorites = currentFavorites.filter(
          (id) => id !== productItemId
        );
      } else {
        updatedFavorites = [...currentFavorites, productItemId];
      }

      setFavoritesInCookies(updatedFavorites);
      return Promise.resolve(updatedFavorites);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["favorites"] });
    },
    onError: (error: Error) => {
      console.error("Error toggling favorite:", error);
    },
  });
};

export const fetchTopProductSales = async (
  month: number,
  year: number,
  limit: number
): Promise<TopProductSales[] | []> => {
  const res = await api.get(
    `/api/v1/shop-order/statistics/topProductSales?month=${month}&year=${year}&limit=${limit}`
  );
  return res.data;
};

export const useTopProductSales = (
  month: number,
  year: number,
  limit: number
) => {
  const { data, error, isLoading, isFetching, isError, refetch } = useQuery<
    TopProductSales[] | [],
    Error
  >({
    queryKey: ["fetchTopProductSales ", month, year, limit],
    queryFn: () => fetchTopProductSales(month, year, limit),
  });

  if (isError && error) {
    console.error("Error fetching top product sales: ", error);
  }

  return { data, error, isLoading, isFetching, isError, refetch };
};

export const fetchLatestSalesProducts = async (
  limit: number
): Promise<LatestSalesProductsResponse[] | []> => {
  const res = await api.get(
    `/api/v1/shop-order/statistics/latestSales?limit=${limit}`
  );
  return res.data;
};

export const useLatestSalesProducts = (limit: number) => {
  const { data, error, isLoading, isFetching, isError, refetch } = useQuery<
    LatestSalesProductsResponse[] | [],
    Error
  >({
    queryKey: ["fetchLatestSalesProducts", limit],
    queryFn: () => fetchLatestSalesProducts(limit),
  });

  if (isError && error) {
    console.error("Error fetching latest sales products: ", error);
  }

  return { data, error, isLoading, isFetching, isError, refetch };
};

export const fetchShopOrderStatistics = async (
  month: number,
  year: number
): Promise<ShopOrderStatistics | null> => {
  const res = await api.get(
    `/api/v1/shop-order/statistics/salesRatio?month=${month}&year=${year}`
  );
  return res.data;
};

export const useShopOrderStatistics = (month: number, year: number) => {
  const { data, error, isLoading, isFetching, isError, refetch } = useQuery<
    ShopOrderStatistics | null,
    Error
  >({
    queryKey: ["fetchShopOrderStatistics", month, year],
    queryFn: () => fetchShopOrderStatistics(month, year),
  });

  if (isError && error) {
    console.error("Error fetching shop order statistics: ", error);
  }

  return { data, error, isLoading, isFetching, isError, refetch };
};

export const fetchStatistics = async (): Promise<Statistics | null> => {
  const res = await api.get(`/api/v1/statistics`);
  return res.data;
};

export const useStatistics = () => {
  const { data, error, isLoading, isFetching, isError, refetch } = useQuery<
    Statistics | null,
    Error
  >({
    queryKey: ["fetchStatistics"],
    queryFn: () => fetchStatistics(),
  });

  if (isError && error) {
    console.error("Error fetching statistics: ", error);
  }

  return { data, error, isLoading, isFetching, isError, refetch };
};
