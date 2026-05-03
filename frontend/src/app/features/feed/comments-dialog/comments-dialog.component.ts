import { Component, OnInit, inject, signal, ViewChild, ElementRef } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import { CommentService, CommentDto, CommentLikeToggleDto } from '../../../core/services/comment.service';

export interface CommentsDialogData {
  postId: number;
  username: string;
  avatarUrl: string;
}

export interface CommentsDialogResult {
  newCommentsCount: number;
}

@Component({
  selector: 'app-comments-dialog',
  standalone: true,
  imports: [MatDialogModule, MatIconModule, FormsModule],
  templateUrl: './comments-dialog.component.html',
  styleUrl: './comments-dialog.component.scss',
})
export class CommentsDialogComponent implements OnInit {
  @ViewChild('commentsList') commentsListRef!: ElementRef<HTMLElement>;

  private readonly commentService = inject(CommentService);
  private readonly dialogRef = inject<MatDialogRef<CommentsDialogComponent, CommentsDialogResult>>(MatDialogRef);
  readonly data = inject<CommentsDialogData>(MAT_DIALOG_DATA);

  comments = signal<CommentDto[]>([]);
  loading = signal(true);
  submitting = signal(false);
  errorMessage = signal<string | null>(null);
  newCommentText = '';

  ngOnInit(): void {
    this.dialogRef.backdropClick().subscribe(() => this.close());

    this.commentService.getComments(this.data.postId).subscribe({
      next: (comments) => {
        this.comments.set(comments);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('No se pudieron cargar los comentarios.');
      },
    });
  }

  submit(): void {
    const content = this.newCommentText.trim();
    if (!content || this.submitting()) return;

    this.submitting.set(true);
    this.errorMessage.set(null);

    this.commentService.addComment(this.data.postId, content).subscribe({
      next: (comment) => {
        this.comments.update(list => [...list, comment]);
        this.newCommentText = '';
        this.submitting.set(false);
        this.scrollToBottom();
      },
      error: () => {
        this.submitting.set(false);
        this.errorMessage.set('No se pudo publicar el comentario.');
      },
    });
  }

  toggleLike(comment: CommentDto): void {
    const optimisticLiked = !comment.likedByCurrentUser;
    const optimisticCount = comment.likesCount + (comment.likedByCurrentUser ? -1 : 1);

    this.comments.update(list =>
      list.map(c =>
        c.id === comment.id
          ? { ...c, likedByCurrentUser: optimisticLiked, likesCount: optimisticCount }
          : c,
      ),
    );

    this.commentService.toggleCommentLike(this.data.postId, comment.id).subscribe({
      next: (res: CommentLikeToggleDto) => {
        this.comments.update(list =>
          list.map(c =>
            c.id === comment.id
              ? { ...c, likedByCurrentUser: res.liked, likesCount: res.likesCount }
              : c,
          ),
        );
      },
      error: () => {
        this.comments.update(list =>
          list.map(c =>
            c.id === comment.id
              ? { ...c, likedByCurrentUser: comment.likedByCurrentUser, likesCount: comment.likesCount }
              : c,
          ),
        );
      },
    });
  }

  close(): void {
    this.dialogRef.close({ newCommentsCount: this.comments().length });
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      const el = this.commentsListRef?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    }, 0);
  }
}
