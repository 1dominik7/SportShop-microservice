import React, { useState } from "react";

const ImageLoader = ({
  src,
  alt,
  className,
}: {
  src: string;
  alt: string;
  className: string;
}) => {
  const [loaded, setLoaded] = useState(false);

  return (
    <>
      {!loaded && (
        <div className={`${className} bg-gray-200 animate-pulse`}></div>
      )}
      <img
        className={`${className} ${loaded ? "opacity-100" : "opacity-0"}`}
        src={src}
        alt={alt}
        loading="lazy"
        onLoad={() => setLoaded(true)}
        onError={() => setLoaded(true)}
      />
    </>
  );
};

export default ImageLoader;
