% INPUT
numbins = 10;
data = [1, 2, 3, 7];

% Function: f(c,xi) = xi + (1-c)(mean - xi)

% Vector
m = mean(data);
p = zeros(numel(data)+1,numbins);
p(1,:) = linspace(0,1,numbins);
for i = 1:numbins
    p(2:numel(data)+1,i) = data + (1-p(1,i)).*(m-data);
end
p
plot(p(1,:),p(2:numel(data)+1,:))
xlabel('Confidence');
ylabel('Likelihood Vector [1,2,3] Scaled');
pause

% Ratios
r = zeros(numel(data)*(numel(data)-3)/2+numel(data),numbins);
k = 1;
for i = 2:numel(data)+1
    for j = i+1:numel(data)+1
        r(k,:) = p(i,:) ./ p(j,:);
        k = k + 1;
    end
end
r
plot(p(1,:),r)
xlabel('Confidence');
ylabel('Ratios between entries');
pause

% Std. Dev.
sd = std(p(2:end,:))
plot(p(1,:),sd)
xlabel('Confidence');
ylabel('Standard Deviation');
