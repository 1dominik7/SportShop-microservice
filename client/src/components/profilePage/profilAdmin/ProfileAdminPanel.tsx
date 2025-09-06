import ProfileAdminPanelLeft from "./ProfileAdminPanelLeft";
import { Outlet } from "react-router";

const ProfileAdminPanel = () => {
  return (
    <div className="flex h-full">
      <div className="h-full w-[100%] flex gap-2 max-lg:flex-col">
        <ProfileAdminPanelLeft/>
        <div className="w-[1px] bg-gray-200 z-30 max-h-full max-lg:w-full max-lg:h-[1px]"/>
          <Outlet />
      </div>
    </div>
  );
};

export default ProfileAdminPanel;
