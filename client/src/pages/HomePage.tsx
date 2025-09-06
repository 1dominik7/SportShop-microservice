import BannerHome from "../components/homePage/BannerHome";
import BannerCenter from "../components/homePage/BannerCenter";

const HomePage = () => {

  return (
    <div className="flex flex-col">
      <div className="h-screen">
        <BannerHome />
      </div>
      <div>
        <BannerCenter />
      </div>
    </div>
  );
};

export default HomePage;
