import { checkAuth } from "./../state/authActions";
export interface User {
  id: number;
  email: string;
  password?: string;
  firstname?: string;
  lastname?: string;
  fullName: string;
  dateOfBirth: string | null;
  createdDate?: string;
  enabled: boolean;
  accountName: boolean;
  accountLocked?: boolean;
  roleNames: string[];
}

export interface UserState {
  user: User | null;
  loading: boolean;
  error: string | null;
  profileUpdated: boolean;
}

export interface SignUpPayload {
  email: string;
  firstname: string;
  lastname: string;
  password: string;
}

export interface SignInPayload {
  email: string;
  password: string;
}

export interface ForgotPasswordType {
  email: string;
}

export interface RestartPasswordType {
  newPassword: string;
  confirmPassword: string;
}

export interface ProfileChangeType {
  firstname: string;
  lastname: string;
  dateOfBirth: string | null;
}

export interface ProfileType {
  id: number;
  firstname: string;
  lastname: string;
  dateOfBirth: string;
  email: string;
  accountLocked: boolean;
  enabled: boolean;
  createdDate: string;
  lastModifiedDate: string;
  roles: [];
  addresses: Address[];
  shoppingCarts: ShoppingCartItem[];
  userPaymentMethods: UserPaymentMethod[];
}

export interface ProductImage {
  id?: number;
  imageFilename: string;
}

export interface VariationOption {
  id: number;
  value: string;
  variation?: Variation;
}

export interface Variation {
  id: number;
  categoryId: number | null;
  name: string;
  options: VariationOption[];
  categoryName?: string;
}

export interface Category {
  id?: number;
  categoryName: string;
  parentCategoryId: number | null;
  variations?: Variation[];
}

export interface ProductItem {
  id?: number;
  price: number;
  discount: number;
  productCode: string;
  productName?: string;
  qtyInStock: number | null;
  variations: Variation[];
  variationOptionIds?: number[];
  variationOptions?: VariationOption[];
  productImages: ProductImage[];
  productDescription?: string;
  categoryId?: number;
  productId?: number;
  otherProductItems?: OtherProductItems[];
  size?: string;
}

export interface Product {
  id?: number;
  productName: string;
  description: string;
  categoryId: number | null;
  productItems: ProductItem[];
  createdDate?: Date;
}

export interface FamousShoes {
  totalElements: number;
}

export interface ProductByCategoryInfo {
  categoryId: number;
  selectedOption: Record<number, { option: number[] }>;
  pageNumber: number;
  pageSize: number;
  sortBy: string;
  sortOrder: string;
}

export interface ProductItemList {
  content: ProductItem[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  lastPage: boolean;
}

export interface Filter {
  categoryId: number | null;
  id: number;
  name: string;
  options: VariationOption[];
}

export interface OtherProductItems {
  id: number;
  price: number;
  discount: number;
  productCode: string;
  qtyInStock: number;
  variationOptionIds?: number | null;
  productImages?: ProductImage[];
}

export interface ProductItemListGroupedByFilters {
  content: ProductItemListGrouped[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  lastPage: boolean;
  limit?: number;
}

export interface ProductItemListGrouped {
  productId: number;
  productName?: string;
  productImages: ProductImage[];
  variations: Variation[];
  productItemRequests: ProductItemGrouped[];
  colour?: string;
  size?: string;
}

export interface ProductItemGrouped {
  id: number;
  price: number;
  discount: number;
  productCode: string;
  qtyInStock: number | null;
  variations: Variation[];
  productImages: ProductImage[];
  productId: number;
  productName: string;
  colour?: string;
  size?: string;
}

export interface ProductItemByColour {
  id: number;
  productId: number;
  productItemId: number;
  productName: string;
  colour: string;
  productItemOneByColour: ProductItemOneByColour[];
  otherProductItemOneByColours: OtherProductItemOneByColours[];
  productImages: ProductImage[];
}

export interface ProductItemOneByColour {
  categoryId: number;
  productId: number;
  colour: string;
  discount: number;
  id: number;
  price: number;
  productCode: string;
  productDescription: string;
  productImages: ProductImage[];
  productName: string;
  qtyInStock: number;
  variations: Variation[];
}

export interface OtherProductItemOneByColours {
  productId: number;
  productItemId: number;
  productName: string;
  colour: string;
  productImages: ProductImage[];
  otherColourVariation: [];
}

export interface ProductQuery {
  id: number;
  productItemId: number;
  imageUrl: string;
  productName: string;
  price: number;
  discount: number;
  colour: string;
}

export interface ToastOptions {
  position:
    | "top-left"
    | "top-right"
    | "top-center"
    | "bottom-left"
    | "bottom-right"
    | "bottom-center";
  autoClose: number;
  hideProgressBar: boolean;
  closeOnClick: boolean;
  pauseOnHover: boolean;
}

export interface ProductItemsFilters {
  categoryId?: number;
  variation: Variation;
}

export interface CartItem {
  id?: number;
  shoppingCartItems: ShoppingCartItem[];
  discountCodes?: DiscountCode[];
}

export interface ShoppingCartItem {
  id?: number;
  productItem: ProductItemByColour;
  qty: number;
}

export interface Address {
  id?: number;
  country: string;
  city: string;
  firstName: string;
  lastName: string;
  postalCode: string;
  street: string;
  phoneNumber: string;
  addressLine1: string;
  addressLine2: string;
}

export interface DiscountCode {
  id: number;
  name: string;
  code: string;
  expiryDate: string;
  discount: number;
}

export interface ShippingMethod {
  id: number;
  name: string;
  price: number;
}

export interface PaymentType {
  id: number;
  value: string;
}

export interface UserReview {
  id?: number;
  userId: number;
  orderLineId: number;
  ratingValue: number;
  comment: string;
  createdDate: string;
  productItemId?: number;
  userName?: string;
}

export interface OrderLine {
  id?: number;
  productName: string;
  productItem: ProductItem;
  shopOrderId: number;
  qty: number;
  price: number;
  userReviews: UserReview[];
}

export interface CreateShopOrder {
  id?: number;
  userId: number;
  orderDate: string;
  paymentMethodId: number;
  addressRequest: Address;
  cartId: number;
  shippingMethodId: number;
  orderTotal: number;
  finalOrderTotal: number;
  appliedDiscountValue: number;
}

export interface UserPaymentMethod {
  id: number;
  user?: User;
  paymentType?: PaymentType;
  provider: string;
  last4CardNumber: string;
  paymentDate: string;
  expiryDate: string;
  isDefault: boolean;
  payments?: Payment[];
}

export interface Payment {
  id: number;
  transactionId: string;
  shopOrder?: ShopOrder;
  paymentMethod: number | UserPaymentMethod;
  createdAt: string;
  updatedAt: string;
  status: string;
}

export interface OrderStatus {
  id: number;
  status: string;
}

export interface ShopOrder {
  id: number;
  userId: number;
  orderDate: string;
  userPaymentMethod: UserPaymentMethod;
  address: Address;
  shippingMethodId: number;
  shippingMethod?: ShippingMethod;
  orderTotal: number;
  finalOrderTotal: number;
  orderStatusId: number;
  orderLines: OrderLine[];
  appliedDiscountValue: number;
  paymentIntentId: string;
}

export interface GetShopOrder {
  id: number;
  userId: string;
  appliedDiscountValue: number;
  finalOrderTotal: number;
  orderDate: string;
  orderLines: OrderLine[];
  orderStatus: OrderStatus;
  orderTotal: number;
  paymentCreatedAt: string;
  paymentId: number;
  paymentIntentId: number;
  paymentMethodName: string;
  paymentStatus: string;
  paymentTransactionId: string;
  shippingCity: string;
  shippingCountry: string;
  shippingFirstName: string;
  shippingLastName: string;
  shippingMethod: ShippingMethod;
  shippingPhoneNumber: string;
  shippingPostalCode: string;
  shippingStreet: string;
  shippingAddressLine1: string;
  shippingAddressLine2: string;
}

export interface ReviewRate {
  productId: number;
  averageRating: number;
  totalReviews: number;
}

export interface UserReviewProductById {
  productId: number;
  reviews: UserReview[];
  averageRating: number;
  totalReviews: number;
}

export interface OrderDetails {
  id: number;
  shopOrder: ShopOrder;
  paymentMethod: UserPaymentMethod;
  transaction: string;
  status: string;
  createdAt: string;
  updatedAt: null | string;
}

export interface OrderList {
  content: GetShopOrder[];
  size: number;
  totalElements: number;
  sort: string[];
  totalPages: number;
  last: boolean;
}

export interface UserList {
  content: User[];
  size: number;
  totalElements: number;
  sort: string[];
  totalPages: number;
  last: boolean;
}

export interface UpdateUser {
  firstname: string;
  lastname: string;
  newPassword?: string;
  dateOfBirth: string;
  accountLocked: boolean;
  enabled: boolean;
  roleIds?: string[];
}

export interface Role {
  id: string;
  name: string;
}

export interface Statistics {
  usersStatistics: UsersStatistics;
  totalProducts: number;
  totalOrders: number;
  totalIncomes: number;
}

export interface UsersStatistics {
  totalUsers: number;
  usersInCurrentMonth: number;
  usersInLastMonth: number;
  usersTwoMonthsAgo: number;
}

export interface OrderStatusStatistics {
  statusName: string;
  count: number;
}

export interface ShopOrderStatistics {
  thisMonthWeeklySales: number[];
  lastMonthWeeklySales: number[];
}

export interface LatestSalesProductsResponse {
  productId: number;
  productItemId: number;
  productName: string;
  orderId: number;
  orderCreatedDate: string;
  price: number;
  quantity: number;
  orderStatus: string;
}

export interface TopProductSales {
  productItem: ProductItem;
  totalQuantity: number;
}
