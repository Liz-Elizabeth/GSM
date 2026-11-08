import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  reactStrictMode: true,
  distDir: ".next",
  experimental: {
    cpus: 1,
  },
};

export default nextConfig;
