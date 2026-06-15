export interface PostListItem {
  postId: number;
  teamCode: string;
  title: string;
  authorNickname: string;
  viewCount: number;
  likeCount: number;
  commentCount: number;
  createdAt: string;
}

export interface PostListResponse {
  items: PostListItem[];
  page: number;
  hasNext: boolean;
}

export interface PostDetail {
  postId: number;
  teamCode: string;
  title: string;
  content: string;
  authorId: number;
  authorNickname: string;
  viewCount: number;
  likeCount: number;
  commentCount: number;
  createdAt: string;
  mine: boolean;
}
