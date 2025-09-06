import { useAppSelector } from "../state/store";

const useIsAdmin = (): boolean => {
  const user = useAppSelector((store) => store.auth.user);

  if (!user || !Array.isArray(user.roleNames)) {
    return false;
  }

  return user.roleNames.some((role) => role?.toLowerCase() === "admin");
};

export default useIsAdmin;
