import { ComponentType } from "react";
import { useAppSelector } from "../../state/store";
import { Navigate } from "react-router";

interface ProtectedRouteProps {
    element: ComponentType; 
    redirectTo?: string; 
  }

const ProtectedRoute = ({ element: Element, redirectTo = "/" }: ProtectedRouteProps) => {
  const isLoggedIn = useAppSelector((store) => store.auth.isLoggedIn);

  if (isLoggedIn) {
    return <Navigate to={redirectTo} replace />;
  }

  return <Element />; 
};

export default ProtectedRoute;
