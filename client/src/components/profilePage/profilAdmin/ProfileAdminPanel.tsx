import ProfileAdminPanelLeft from "./ProfileAdminPanelLeft";
import { Outlet } from "react-router";

const ProfileAdminPanel = () => {
  return (
    <div className="flex h-full">
      <div className="h-full w-[100%] flex max-lg:flex-col">
        <div className="bg-blue-950">
        <ProfileAdminPanelLeft/>
        </div>
          <Outlet />
      </div>
    </div>
  );
};

export default ProfileAdminPanel;
