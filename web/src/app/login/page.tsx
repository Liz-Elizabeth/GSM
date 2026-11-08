import { Suspense } from "react";
import { LoginView } from "@/components/auth/LoginView";

export const dynamic = "force-dynamic";

export default function LoginPage() {
  return (
    <Suspense fallback={<div className="min-h-screen bg-[#F2F2F7]" />}>
      <LoginView />
    </Suspense>
  );
}
