"use client";

import React from "react";

export function TicketSkeletonCard() {
  return (
    <div className="bg-white rounded-2xl p-5 border border-[#E5E5EA] shadow-xs flex flex-col gap-3">
      <div className="flex items-center justify-between">
        <div className="h-4 w-28 rounded-md animate-shimmer" />
        <div className="flex gap-2">
          <div className="h-6 w-20 rounded-full animate-shimmer" />
          <div className="h-6 w-16 rounded-full animate-shimmer" />
        </div>
      </div>
      <div className="h-6 w-3/4 rounded-md animate-shimmer" />
      <div className="h-4 w-full rounded-md animate-shimmer" />
      <div className="h-4 w-2/3 rounded-md animate-shimmer" />
      <div className="pt-2 border-t border-[#F2F2F7] flex items-center justify-between">
        <div className="h-4 w-32 rounded-md animate-shimmer" />
        <div className="h-4 w-24 rounded-md animate-shimmer" />
      </div>
    </div>
  );
}

export function TicketSkeletonList({ count = 4 }: { count?: number }) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
      {Array.from({ length: count }).map((_, idx) => (
        <TicketSkeletonCard key={idx} />
      ))}
    </div>
  );
}

export function BoardingPassSkeleton() {
  return (
    <div className="bg-white rounded-3xl overflow-hidden border border-[#E5E5EA] shadow-xs flex flex-col">
      <div className="bg-[#0D253F] h-10 w-full animate-shimmer opacity-40" />
      <div className="p-6 flex flex-col gap-4">
        <div className="flex justify-between items-center">
          <div className="h-8 w-16 rounded-md animate-shimmer" />
          <div className="h-4 w-24 rounded-md animate-shimmer" />
          <div className="h-8 w-16 rounded-md animate-shimmer" />
        </div>
        <div className="h-5 w-3/4 rounded-md animate-shimmer" />
        <div className="h-10 w-full rounded-xl animate-shimmer" />
      </div>
    </div>
  );
}
