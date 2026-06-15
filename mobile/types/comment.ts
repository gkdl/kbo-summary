export interface Comment {
  commentId: number;
  parentId: number | null;
  content: string;
  authorId: number;
  authorNickname: string;
  createdAt: string;
  deleted: boolean;
  mine: boolean;
  replies: Comment[];
}

export interface LikeResult {
  liked: boolean;
  likeCount: number;
}
