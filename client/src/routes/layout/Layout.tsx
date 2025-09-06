import Navbar from "../../components/Navbar";
import { Navigate, Outlet } from "react-router";
import { useAppSelector } from "../../state/store";

const Layout = () => {
  return (
    <div className="flex flex-col">
      <div>
        <Navbar />
      </div>
      <div>
        <Outlet />
      </div>
    </div>
  );
};

const RequireAuth = () => {
  const isLoggedIn = useAppSelector((store) => store.auth.isLoggedIn);

  if (!isLoggedIn) {
    return <Navigate to="/login" />;
  }

  return (
    isLoggedIn && (
      <div className="flex flex-col">
        <div>
          <Navbar />
        </div>
        <div>
          <Outlet />
        </div>
      </div>
    )
  );
};

const RequireAdmin = () => {
  const user = useAppSelector((store) => store.auth.user);
  const isLoggedIn = useAppSelector((store) => store.auth.isLoggedIn);

  const isAdmin =
    isLoggedIn &&
    Array.isArray(user?.roleNames) &&
    user.roleNames.some((role) => role.toLowerCase() === "admin");

  if (!isAdmin) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
};

export { Layout, RequireAuth, RequireAdmin };
