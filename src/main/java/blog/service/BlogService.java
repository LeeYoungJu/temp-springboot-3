package blog.service;

import blog.domain.Article;
import blog.dto.AddArticleRequest;
import blog.dto.UpdateArticleRequest;
import blog.repository.BlogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BlogService {

  private final BlogRepository blogRepository;

  public Article save(AddArticleRequest request, String userName) {
    return blogRepository.save(request.toEntity(userName));
  }

  public List<Article> findAll() {
    return blogRepository.findAll();
  }

  public Article findById(long id) {
    return blogRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("not found: " + id));
  }

  public void delete(long id) {
    Article article = blogRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("not found : " + id));

    authorizeArticleAuthor(article);
    blogRepository.deleteById(id);
  }

  @Transactional
  public Article update(long id, UpdateArticleRequest request) {
    Article article = blogRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("not found: " + id));

    authorizeArticleAuthor(article);
    article.update(request.getTitle(), request.getContent());

    return article;
  }

  private static void authorizeArticleAuthor(Article article) {
    String userName = SecurityContextHolder.getContext().getAuthentication().getName();
    if(!article.getAuthor().equals(userName)) {
      throw new IllegalArgumentException("not authorized");
    }
  }
}
