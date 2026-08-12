import { Component, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { FeedPostDto, FeedPostType } from '../../../core/services/feed.service';
import { PostContentComponent, PostContentPost } from '../post-content/post-content.component';
import {
  GOAL_CREATED_DEMO_VARIANTS,
  MOCK_GOAL_CREATED_JOINED,
} from './goal-created.mock';

interface DemoPostView extends PostContentPost {
  userId: number;
  username: string;
  avatarUrl: string;
  creationDate: string;
  likes: number;
  liked: boolean;
  commentsCount: number;
  participantsCount: number;
  joinedByCurrentUser: boolean;
  isOwner: boolean;
  variantLabel: string;
}

@Component({
  selector: 'app-goal-created-demo',
  standalone: true,
  imports: [MatIconModule, PostContentComponent],
  templateUrl: './goal-created-demo.component.html',
  styleUrl: './goal-created-demo.component.scss',
})
export class GoalCreatedDemoComponent {
  readonly variants = GOAL_CREATED_DEMO_VARIANTS;

  posts = signal<DemoPostView[]>(
    GOAL_CREATED_DEMO_VARIANTS.map(v => this.mapMock(v.mock, v.isOwner, v.label))
  );

  toggleLike(post: DemoPostView): void {
    this.posts.update(list =>
      list.map(p =>
        p.id === post.id
          ? { ...p, liked: !p.liked, likes: p.liked ? p.likes - 1 : p.likes + 1 }
          : p
      )
    );
  }

  toggleJoin(post: DemoPostView): void {
    if (post.isOwner || post.joinedByCurrentUser) return;

    this.posts.update(list =>
      list.map(p => {
        if (p.id !== post.id) return p;
        const joined = MOCK_GOAL_CREATED_JOINED;
        return this.mapMock(joined, false, p.variantLabel);
      })
    );
  }

  typeLabel(postType: FeedPostType): string {
    if (postType === 'GOAL_CREATED' || postType === 'GOAL_JOIN') return 'Objetivo';
    return postType;
  }

  formatPostDate(dateStr: string): string {
    const d = new Date(dateStr);
    if (Number.isNaN(d.getTime())) return '';
    return d.toLocaleDateString('es-ES', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  }

  private mapMock(dto: FeedPostDto, isOwner: boolean, variantLabel: string): DemoPostView {
    return {
      id: dto.id,
      userId: dto.userId,
      username: dto.username,
      avatarUrl: dto.photoUrl ?? `https://i.pravatar.cc/48?u=${dto.userId}`,
      description: dto.description ?? '',
      postType: dto.postType,
      title: dto.title ?? null,
      trainingTag: dto.trainingTag,
      challengeDeadline: dto.challengeDeadline ?? null,
      creationDate: dto.creationDate,
      participantsCount: dto.participantsCount ?? dto.goal?.participantsCount ?? 0,
      joinedByCurrentUser: dto.goal?.joinedByCurrentUser ?? dto.joinedByCurrentUser ?? false,
      likes: dto.likesCount ?? 0,
      liked: dto.likedByCurrentUser ?? false,
      commentsCount: dto.commentsCount ?? 0,
      streakWeeks: dto.streakWeeks ?? null,
      weekDayTags: dto.weekDayTags ?? null,
      wodPostId: dto.wodPostId ?? null,
      wodTitle: dto.wodTitle ?? null,
      wodCheckinsCount: dto.wodCheckinsCount ?? null,
      wodCheckinAuthors: dto.wodCheckinAuthors ?? null,
      goalMark: dto.goalMark ?? null,
      goal: dto.goal ?? null,
      isOwner,
      variantLabel,
    };
  }
}
