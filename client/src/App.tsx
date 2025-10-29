import { createBrowserRouter, RouterProvider } from "react-router";
import "./App.css";
import Home from "./pages/HomePage";
import Login from "./pages/auth/Login";
import Register from "./pages/auth/Register";
import VerifyAccount from "./pages/auth/VerifyAccount";
import { RequireAuth, Layout, RequireAdmin } from "./routes/layout/Layout";
import ProfileAddresses from "./components/profilePage/profileAddresses/ProfileAddresses";
import ProfileOrders from "./components/profilePage/ProfileOrders";
import ProfileAdminPanel from "./components/profilePage/profilAdmin/ProfileAdminPanel";
import ProfileAdminProducts from "./components/profilePage/profilAdmin/ProfileAdminProducts";
import ProfileAdminAddProducts from "./components/profilePage/profilAdmin/ProfileAdminAddProducts";
import ProfileAdminCategories from "./components/profilePage/profilAdmin/ProfileAdminCategories";
import ProfileAdminVariation from "./components/profilePage/profilAdmin/ProfileAdminVariation";
import ProfileAdminVariationOptions from "./components/profilePage/profilAdmin/ProfileAdminVariationOptions";
import EditProduct from "./pages/EditProductPage";
import ProfilePage from "./pages/ProfilePage";
import ProductsPage from "./pages/ProductsPage";
import ProductItemPage from "./pages/ProductItemPage";
import ProtectedRoute from "./routes/protectedRoutes/ProtectedRoute";
import BasketPage from "./pages/BasketPage";
import ProfileAdminDiscount from "./components/profilePage/profilAdmin/ProfileAdminDiscount";
import PaymentSuccess from "./pages/PaymentSuccess";
import PaymentFailure from "./pages/PaymentFailure";
import ProfileOrderDetails from "./components/profilePage/ProfileOrderDetails";
import ReviewPage from "./components/profilePage/ReviewPage";
import FavoritePage from "./pages/FavoritePage";
import ResetPassword from "./pages/auth/ResetPassword";
import ForgotPassword from "./pages/auth/ForgotPassword";
import ProfilePersonalData from "./components/profilePage/ProfilePersonalData";
import NotFound from "./pages/NotFound";
import useTokenRefresh from "./config/useTokenRefresh";
import ProfileAdminOrders from "./components/profilePage/profilAdmin/ProfileAdminOrders";
import ProfileAdminUsers from "./components/profilePage/profilAdmin/ProfileAdminUsers";
import ProfileAdminUser from "./components/profilePage/profilAdmin/ProfileAdminUser";
import ProfileAdminStatistics from "./components/profilePage/profilAdmin/ProfileAdminStatistics";
import ProfileAdminShippingMethods from "./components/profilePage/profilAdmin/ProfileAdminShippingMethods";
import ProfileAdminOrderStatuses from "./components/profilePage/profilAdmin/ProfileAdminOrderStatuses";

function App() {
  useTokenRefresh();
  const router = createBrowserRouter([
    {
      element: <Layout />,
      children: [
        {
          path: "/",
          element: <Home />,
        },
        {
          path: "/login",
          element: <ProtectedRoute element={Login} />,
        },
        {
          path: "/register",
          element: <ProtectedRoute element={Register} />,
        },
        {
          path: "/verify-account",
          element: <VerifyAccount />,
        },
        {
          path: "/forgot-password",
          element: <ForgotPassword />,
        },
        {
          path: "/reset-password/:token",
          element: <ResetPassword />,
        },
        {
          path: "/products",
          element: <ProductsPage />,
        },
        {
          path: "/products/:productId",
          element: <ProductItemPage />,
        },
        {
          path: "/favorite",
          element: <FavoritePage />,
        },
        {
          path: "/basket",
          element: <BasketPage />,
        },
        {
          path: "/basket/delivery",
          element: <BasketPage />,
        },
        {
          path: "/payment-success",
          element: <PaymentSuccess />,
        },
        {
          path: "/payment-cancel",
          element: <PaymentFailure />,
        },
        {
          path: "/not-found",
          element: <NotFound />,
        },
        {
          path: "*",
          element: <NotFound />,
        },
        {
          element: <RequireAdmin />,
          children: [
            {
              path: "/products/:productId/edit",
              element: <EditProduct />,
            },
          ],
        },
      ],
    },
    {
      element: <RequireAuth />,
      children: [
        {
          path: "/profile",
          element: <ProfilePage />,
          children: [
            {
              path: "personalData",
              element: <ProfilePersonalData />,
            },
            {
              path: "addresses",
              element: <ProfileAddresses />,
            },
            {
              path: "orders",
              element: <ProfileOrders />,
            },
            {
              path: "orders/:orderId",
              element: <ProfileOrderDetails />,
            },
            {
              path: "orders/review/:orderId",
              element: <ReviewPage />,
            },
            {
              element: <RequireAdmin />,
              children: [
                {
                  path: "adminPanel",
                  element: <ProfileAdminPanel />,
                  children: [
                    { path: "products", element: <ProfileAdminProducts /> },
                    {
                      path: "addProduct",
                      element: <ProfileAdminAddProducts />,
                    },
                    { path: "categories", element: <ProfileAdminCategories /> },
                    { path: "variations", element: <ProfileAdminVariation /> },
                    { path: "discounts", element: <ProfileAdminDiscount /> },
                    {
                      path: "variationOptions",
                      element: <ProfileAdminVariationOptions />,
                    },
                    { path: "orders", element: <ProfileAdminOrders /> },
                    {
                      path: "orders/:orderId",
                      element: <ProfileOrderDetails />,
                    },
                    {
                      path: "users",
                      element: <ProfileAdminUsers />,
                    },
                    {
                      path: "users/:userId",
                      element: <ProfileAdminUser />,
                    },
                    {
                      path: "statistics",
                      element: <ProfileAdminStatistics />,
                    },
                    {
                      path: "shippingMethods",
                      element: <ProfileAdminShippingMethods />,
                    },
                    {
                      path: "orderStatuses",
                      element: <ProfileAdminOrderStatuses />,
                    },
                  ],
                },
              ],
            },
          ],
        },
      ],
    },
  ]);

  return <RouterProvider router={router} />;
}

export default App;
