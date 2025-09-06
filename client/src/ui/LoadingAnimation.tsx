import { useLottie } from "lottie-react";
import loadingAnimation from "../utils/loadingAnimation.json";

interface LoadingAnimationProps {
  className?: string;
  height?: number;
  width?: number;
}

const LoadingAnimation = ({
  className = "",
  height = 200,
  width = 200,
}: LoadingAnimationProps) => {
  const styles = {
    height,
    width,
  };

  const defaultOptions = {
    loop: true,
    autoplay: true,
    animationData: loadingAnimation,
    rendererSettings: {
      preserveAspectRatio: "xMidYMid slice",
    },
  };

  const { View } = useLottie(defaultOptions, styles);

  return <div className={className}>{View}</div>;
};

export default LoadingAnimation;
