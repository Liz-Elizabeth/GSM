import { Client, Storage } from "appwrite";

export const APPWRITE_CONFIG = {
  endpoint: process.env.NEXT_PUBLIC_APPWRITE_ENDPOINT || "https://nyc.cloud.appwrite.io/v1",
  projectId: process.env.NEXT_PUBLIC_APPWRITE_PROJECT_ID || "6ae91595000d83e34686",
  bucketId: process.env.NEXT_PUBLIC_APPWRITE_BUCKET_ID || "6ae915ad002bdc1515ec",
};

// Singleton Appwrite client initialization
export const appwriteClient = new Client()
  .setEndpoint(APPWRITE_CONFIG.endpoint)
  .setProject(APPWRITE_CONFIG.projectId);

export const appwriteStorage = new Storage(appwriteClient);
